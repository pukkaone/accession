package com.github.pukkaone.accession.schema.registry;

import com.github.pukkaone.accession.schema.registry.configuration.Registration;
import com.github.pukkaone.accession.schema.registry.rule.RuleViolation;
import com.github.pukkaone.accession.schema.registry.rule.SchemaValidator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType;
import org.apache.avro.SchemaNormalization;

/**
 * Puts subjects and versions in working tree.
 */
public class WritableSchemaRegistry {

  private static final char ID_SEPARATOR = '_';
  private static final int INVALID_ID = -1;
  private static final int MIN_SUBJECT_ID = 0;
  private static final int MAX_SUBJECT_ID = 0x3FFFFF;
  private static final int MIN_VERSION = 0;
  private static final int MAX_VERSION = 999;

  private Path subjectsDir;
  private SchemaBuilder schemaBuilder;
  private Map<Integer, Path> subjectIdToDirectoryMap;
  private Map<String, Integer> subjectNameToSubjectIdMap;

  /**
   * Constructor.
   *
   * @param subjectsDir
   *     directory containing subject subdirectories
   * @param schemaBuilder
   *     schema builder
   */
  public WritableSchemaRegistry(Path subjectsDir, SchemaBuilder schemaBuilder) {
    this.subjectsDir = subjectsDir;
    this.schemaBuilder = schemaBuilder;

    try (var stream = subjectDirectoryStream()) {
      subjectIdToDirectoryMap = stream
          .filter(entry -> extractSubjectId(entry) != INVALID_ID)
          .collect(Collectors.toMap(
              WritableSchemaRegistry::extractSubjectId, Function.identity()));
    }

    subjectNameToSubjectIdMap = subjectIdToDirectoryMap.entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> extractSubjectName(entry.getValue()),
            Map.Entry::getKey));
  }

  private static boolean isSubjectDirectory(Path candidateDir) {
    return Files.isDirectory(candidateDir) &&
        candidateDir.getFileName().toString().charAt(0) != '.';
  }

  private Stream<Path> subjectDirectoryStream() {
    try {
      return Files.list(subjectsDir)
          .filter(WritableSchemaRegistry::isSubjectDirectory);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read subjects directory " + subjectsDir, e);
    }
  }

  private static int extractSubjectId(Path entry) {
    String fileName = entry.getFileName().toString();
    int idEnd = fileName.indexOf(ID_SEPARATOR);
    if (idEnd < 0) {
      return INVALID_ID;
    }

    try {
      return Integer.parseInt(fileName.substring(0, idEnd));
    } catch (NumberFormatException e) {
      return INVALID_ID;
    }
  }

  private static String extractSubjectName(Path entry) {
    String fileName = entry.getFileName().toString();
    int idEnd = fileName.indexOf(ID_SEPARATOR);
    if (idEnd < 0) {
      throw new IllegalStateException("Cannot extract subject name from " + fileName);
    }

    return fileName.substring(idEnd + 1);
  }

  private static int extractVersion(Path entry) {
    String fileName = entry.getFileName().toString();
    int versionEnd = fileName.indexOf('.');
    if (versionEnd < 0) {
      return INVALID_ID;
    }

    try {
      return Integer.parseInt(fileName.substring(0, versionEnd));
    } catch (NumberFormatException e) {
      return INVALID_ID;
    }
  }

  private static int hash(String subjectName) {
    var crc32 = new CRC32();
    crc32.update(subjectName.getBytes(StandardCharsets.UTF_8));
    return (int) crc32.getValue() & MAX_SUBJECT_ID;
  }

  private int determineSubjectId(String subjectName) {
    var existingSubjectId = subjectNameToSubjectIdMap.get(subjectName);
    if (existingSubjectId != null) {
      return existingSubjectId;
    }

    int newSubjectId = hash(subjectName);
    while (subjectIdToDirectoryMap.containsKey(newSubjectId)) {
      if (++newSubjectId > MAX_SUBJECT_ID) {
        newSubjectId = MIN_SUBJECT_ID;
      }
    }

    return newSubjectId;
  }

  private Path createSubjectDirectory(int subjectId, String subjectName) {
    String subDirName = String.format("%07d_%s", subjectId, subjectName);
    Path subjectDir = subjectsDir.resolve(subDirName);
    try {
      Files.createDirectory(subjectDir);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create subject directory " + subjectDir, e);
    }

    subjectIdToDirectoryMap.put(subjectId, subjectDir);
    subjectNameToSubjectIdMap.put(subjectName, subjectId);
    return subjectDir;
  }

  private static Stream<Path> schemaFileStream(Path subjectDir) {
    try {
      return Files.list(subjectDir)
          .filter(Files::isRegularFile);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read directory " + subjectDir, e);
    }
  }

  private static Path findSchemaFileByVersion(Registration registration, Path subjectDir) {
    String fileName = String.format("%03d.avsc", registration.getVersion());
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

    Optional<Path> oldSchemaFile = schemaFileStream(subjectDir)
        .filter(entry -> {
          int version = extractVersion(entry);
          return version != INVALID_ID && version < registration.getVersion();
        })
        .max(Comparator.comparingInt(WritableSchemaRegistry::extractVersion));
    if (oldSchemaFile.isPresent()) {
      Schema oldSchema = SchemaBuilder.readSchemaFile(oldSchemaFile.get());
      var compatibility = compatibilityDirection.check(newSchema, oldSchema);
      if (compatibility.getType() == SchemaCompatibilityType.INCOMPATIBLE) {
        String message = String.format(
            "In subject %s, new schema is not %s compatible with existing version %d\n",
            registration.getSubject(),
            compatibilityDirection,
            extractVersion(oldSchemaFile.get()));
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
   *     whether to allow replacing schema of existing version
   * @return existing version is unchanged, add new version, or replace schema of existing version
   */
  public VersionAction register(
      Registration registration,
      CompatibilityDirection compatibilityDirection,
      boolean mutableVersions) {

    if (registration.getVersion() < MIN_VERSION) {
      throw new IllegalArgumentException(String.format(
          "Version %d is less than minimum allowed version %d",
          registration.getVersion(),
          MIN_VERSION));
    }

    if (registration.getVersion() > MAX_VERSION) {
      throw new IllegalArgumentException(String.format(
          "Version %d is greater than maximum allowed version %d",
          registration.getVersion(),
          MAX_VERSION));
    }

    Schema newSchema = schemaBuilder.generateSchema(registration);

    var schemaValidator = new SchemaValidator();
    List<RuleViolation> violations = schemaValidator.validate(newSchema);
    if (!violations.isEmpty()) {
      throw new IllegalStateException(violations.get(0).toString());
    }

    VersionAction action = VersionAction.NONE;
    registration.appendValueToSubject();

    int subjectId = determineSubjectId(registration.getSubject());
    Path subjectDir = subjectIdToDirectoryMap.get(subjectId);
    if (subjectDir == null) {
      subjectDir = createSubjectDirectory(subjectId, registration.getSubject());
      action = VersionAction.ADD;
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
