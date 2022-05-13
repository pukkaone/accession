package com.github.pukkaone.accession.schema.registry.maven;

import com.github.pukkaone.accession.schema.registry.maven.rule.RuleViolation;
import com.github.pukkaone.accession.schema.registry.maven.rule.SchemaValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType;
import org.apache.avro.SchemaNormalization;

/**
 * Accesses subjects and versions in working tree.
 */
@RequiredArgsConstructor
public class SchemaRegistry {

  private static final char ID_SEPARATOR = '_';
  private static final int MIN_VERSION = 0;

  private final Path subjectsDir;
  private final SchemaBuilder schemaBuilder;

  private static boolean isValidSubject(Path subjectDir) {
    return Files.isDirectory(subjectDir) &&
        subjectDir.getFileName().toString().charAt(0) != '.';
  }

  private Stream<Path> subjectDirectoryStream() {
    try {
      return Files.list(subjectsDir)
          .filter(SchemaRegistry::isValidSubject);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read subjects directory " + subjectsDir, e);
    }
  }

  private static int extractId(Path entry) {
    String fileName = entry.getFileName().toString();
    int idEnd = fileName.indexOf(ID_SEPARATOR);
    if (idEnd < 0) {
      idEnd = fileName.length();
    }

    try {
      return Integer.parseInt(fileName.substring(0, idEnd));
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private static String extractName(Path entry) {
    String name = entry.getFileName().toString();
    int idEnd = name.indexOf(ID_SEPARATOR);
    if (idEnd < 0) {
      return "";
    }

    return name.substring(idEnd + 1);
  }

  private Path findSubjectDirectoryByName(String subjectName) {
    try (var stream = subjectDirectoryStream()) {
      return stream.filter(subjectDir -> extractName(subjectDir).equals(subjectName))
        .findFirst()
        .orElse(null);
    }
  }

  private int findMaximumExistingSubjectId() {
    try (var stream = subjectDirectoryStream()) {
      return stream.mapToInt(SchemaRegistry::extractId)
          .max()
          .orElse(0);
    }
  }

  private Path addSubject(Registration registration) {
    if (registration.getVersion() < MIN_VERSION) {
      throw new IllegalArgumentException(String.format(
          "Version %d is less than minimum version %d",
          registration.getVersion(),
          MIN_VERSION));
    }

    int subjectId = findMaximumExistingSubjectId() + 1;
    String subDirName = String.format("%04d_%s", subjectId, registration.getSubject());
    Path subjectDir = subjectsDir.resolve(subDirName);
    try {
      Files.createDirectory(subjectDir);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create subject directory " + subjectDir, e);
    }

    return subjectDir;
  }

  private static Stream<Path> schemaFileStream(Path subjectDir, Predicate<? super Path> filter) {
    try {
      return Files.list(subjectDir)
          .filter(filter);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read directory " + subjectDir, e);
    }
  }

  private static Path findSchemaFileByVersion(Registration registration, Path subjectDir) {
    Optional<Path> schemaFile = schemaFileStream(
        subjectDir,
        entry -> Files.isRegularFile(entry) && extractId(entry) == registration.getVersion())
        .findFirst();
    if (schemaFile.isPresent()) {
      return schemaFile.get();
    }

    String fileName = String.format("%04d.avsc", registration.getVersion());
    return subjectDir.resolve(fileName);
  }

  private static boolean isIdentical(Schema newSchema, Schema existingSchema) {
    return SchemaNormalization.parsingFingerprint64(newSchema)
        == SchemaNormalization.parsingFingerprint64(existingSchema);
  }

  private static void checkCompatibility(
      Registration registration,
      Path subjectDir,
      Schema newSchema,
      CompatibilityDirection compatibilityDirection) {

    Optional<Path> oldSchemaFile = schemaFileStream(
        subjectDir,
        entry -> {
          if (!Files.isRegularFile(entry)) {
            return false;
          }

          int version = extractId(entry);
          return version >= 0 && version < registration.getVersion();
        })
        .max(Comparator.comparingInt(SchemaRegistry::extractId));
    if (oldSchemaFile.isPresent()) {
      Schema oldSchema = SchemaBuilder.readSchemaFile(oldSchemaFile.get());
      var compatibility = compatibilityDirection.check(newSchema, oldSchema);
      if (compatibility.getType() == SchemaCompatibilityType.INCOMPATIBLE) {
        String message = String.format(
            "In subject %s, new schema is not %s compatible with existing version %d\n",
            registration.getSubject(),
            compatibilityDirection,
            extractId(oldSchemaFile.get()));
        String incompatibilities = compatibility.getResult()
            .getIncompatibilities()
            .stream()
            .map(incompatibility ->
                incompatibility.getMessage() + " at " + incompatibility.getLocation())
            .collect(Collectors.joining(System.lineSeparator()));
        throw new IllegalStateException(message + incompatibilities);
      }
    }
  }

  /**
   * Puts schema in schema registry.
   *
   * @param registration
   *     registration request
   * @param compatibilityDirection
   *     schema compatibility mode
   * @param mutableVersions
   *     if true, allow replacing existing version schema
   * @return existing version is unchanged, add new version, or replace existing version schema
   */
  public VersionAction register(
      Registration registration,
      CompatibilityDirection compatibilityDirection,
      boolean mutableVersions) {

    VersionAction action = VersionAction.NONE;
    registration.appendValueToSubject();

    Path subjectDir = findSubjectDirectoryByName(registration.getSubject());
    if (subjectDir == null) {
      subjectDir = addSubject(registration);
      action = VersionAction.ADD;
    }

    Schema newSchema = schemaBuilder.generateSchema(registration);

    SchemaValidator checker = new SchemaValidator();
    List<RuleViolation> violations = checker.validate(newSchema);
    if (!violations.isEmpty()) {
      throw new IllegalStateException(violations.get(0).toString());
    }

    Path schemaFile = findSchemaFileByVersion(registration, subjectDir);
    Schema existingSchema = SchemaBuilder.readSchemaFile(schemaFile);
    if (existingSchema == null) {
      action = VersionAction.ADD;
    } else if (!isIdentical(newSchema, existingSchema)) {
      if (!mutableVersions) {
        throw new IllegalStateException(String.format(
            "In subject %s, attempt to change schema of existing version %d is disallowed " +
            "because configuration property mutableVersions is false.",
            registration.getSubject(),
            registration.getVersion()));
      }

      action = VersionAction.REPLACE;
    }

    checkCompatibility(registration, subjectDir, newSchema, compatibilityDirection);
    if (action != VersionAction.NONE) {
      SchemaBuilder.writeSchemaFile(newSchema, schemaFile);
    }

    return action;
  }
}
