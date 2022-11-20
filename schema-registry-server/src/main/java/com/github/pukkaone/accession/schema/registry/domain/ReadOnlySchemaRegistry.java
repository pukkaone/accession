package com.github.pukkaone.accession.schema.registry.domain;

import com.github.pukkaone.accession.schema.registry.dto.SchemaResponse;
import com.github.pukkaone.accession.schema.registry.function.SchemaUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reads subjects and versions in working tree.
 */
@Getter
@Slf4j
public class ReadOnlySchemaRegistry {

  private static final int FACTOR = 1000;
  private static final char ID_SEPARATOR = '_';
  private static final int INVALID_ID = -1;

  private Map<Integer, Path> subjectIdToDirectoryMap = new HashMap<>();
  private Map<String, Path> subjectNameToDirectoryMap = new HashMap<>();

  /**
   * Constructor.
   *
   * @param subjectsDir
   *         directory containing subject subdirectories
   */
  public ReadOnlySchemaRegistry(Path subjectsDir) {
    readSubjects(subjectsDir);
  }

  private static boolean isValidSubject(Path subjectDir) {
    return Files.isDirectory(subjectDir) &&
        !subjectDir.getFileName().toString().startsWith(".");
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
      return "";
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

  private void readSubject(Path subjectDir) {
    int subjectId = extractSubjectId(subjectDir);
    String subjectName = extractSubjectName(subjectDir);
    if (subjectId == INVALID_ID || subjectName.isEmpty()) {
      log.warn("Ignoring invalid directory name [{}]", subjectDir);
      return;
    }

    subjectIdToDirectoryMap.put(subjectId, subjectDir);
    subjectNameToDirectoryMap.put(subjectName, subjectDir);
  }

  private void readSubjects(Path subjectsDir) {
    try {
      DirectoryStream<Path> subjectDirs = Files.newDirectoryStream(
          subjectsDir, ReadOnlySchemaRegistry::isValidSubject);

      for (Path subjectDir : subjectDirs) {
        readSubject(subjectDir);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read subjects", e);
    }
  }

  /**
   * Finds all subject names.
   *
   * @return subjects
   */
  public Set<String> findSubjectNames() {
    return subjectNameToDirectoryMap.keySet();
  }

  private Path getSubjectDirectory(String subjectName) {
    Path subjectDir = subjectNameToDirectoryMap.get(subjectName);
    if (subjectDir == null) {
      throw new NotFoundException("Subject name [" + subjectName + "] not found");
    }

    return subjectDir;
  }

  private static DirectoryStream<Path> streamVersionFiles(Path subjectDir) {
    log.info("Reading directory {}", subjectDir);
    try {
      return Files.newDirectoryStream(
          subjectDir,
          entry -> Files.isRegularFile(entry));
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read directory [" + subjectDir + "]", e);
    }
  }

  private List<Integer> findVersionsBySubject(Path subjectDir) {
    List<Integer> sortedVersions = new ArrayList<>();
    try (DirectoryStream<Path> versionFiles = streamVersionFiles(subjectDir)) {
      for (Path versionFile : versionFiles) {
        int version = extractVersion(versionFile);
        if (version >= 0) {
          sortedVersions.add(version);
        } else {
          log.warn("Ignoring invalid file name [{}]", versionFile);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Error closing DirectoryStream", e);
    }

    sortedVersions.sort(null);
    return sortedVersions;
  }

  /**
   * Finds versions by subject name.
   *
   * @param subjectName
   *     subject name to search for
   * @return versions
   */
  public List<Integer> findVersionsBySubjectName(String subjectName) {
    Path subjectDir = getSubjectDirectory(subjectName);
    return findVersionsBySubject(subjectDir);
  }

  private static Registration toRegistration(Path subjectDir, int version, String schema) {
    return Registration.builder()
        .schemaId(extractSubjectId(subjectDir) * FACTOR + version)
        .subjectName(extractSubjectName(subjectDir))
        .version(version)
        .schema(schema)
        .build();
  }

  private Registration findRegistrationBySubjectAndVersion(Path subjectDir, int version) {
    String fileName = String.format("%03d.avsc", version);
    Path schemaFile = subjectDir.resolve(fileName);
    String schema;
    try {
      schema = Files.readString(schemaFile, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new NotFoundException("Version [" + version + "] not found");
    }

    return toRegistration(subjectDir, version, schema);
  }

  private Registration findRegistrationBySubjectNameAndVersion(String subjectName, int version) {
    return findRegistrationBySubjectAndVersion(getSubjectDirectory(subjectName), version);
  }

  /**
   * Finds registration by subject name and version.
   *
   * @param subjectName
   *     subject name to search for
   * @param version
   *     version to search for
   * @return registration
   */
  public SchemaResponse findSchemaBySubjectNameAndVersion(String subjectName, int version) {
    Registration registration = findRegistrationBySubjectNameAndVersion(subjectName, version);
    return SchemaResponse.builder()
        .id(registration.getSchemaId())
        .name(registration.getSubjectName())
        .version(registration.getVersion())
        .schema(registration.getSchema())
        .build();
  }

  /**
   * Finds latest registration by subject.
   *
   * @param subjectName
   *     subject to search for
   * @return registration
   */
  public SchemaResponse findLatestSchemaBySubjectName(String subjectName) {
    List<Integer> versions = findVersionsBySubjectName(subjectName);
    int latestVersion = versions.get(versions.size() - 1);
    return findSchemaBySubjectNameAndVersion(subjectName, latestVersion);
  }

  /**
   * Finds schema by schema ID.
   *
   * @param schemaId
   *     schema ID to search for
   * @return schema
   */
  public SchemaResponse findSchemaBySchemaId(int schemaId) {
    int subjectId = schemaId / FACTOR;
    Path subjectDir = subjectIdToDirectoryMap.get(subjectId);
    if (subjectDir == null) {
      throw new NotFoundException("Subject ID [" + subjectId + "] not found");
    }

    int version = schemaId % FACTOR;
    Registration registration = findRegistrationBySubjectAndVersion(subjectDir, version);
    return SchemaResponse.builder()
        .schema(registration.getSchema())
        .build();
  }

  /**
   * Finds registration by subject name and schema.
   *
   * @param subjectName
   *     subject name to search for
   * @param schema
   *     schema to search for
   * @return registration
   */
  public Registration findRegistrationBySubjectNameAndSchema(
      String subjectName, String schema) {

    long fingerprint = SchemaUtils.fingerprint(schema);

    Path subjectDir = getSubjectDirectory(subjectName);
    try (DirectoryStream<Path> versionFiles = streamVersionFiles(subjectDir)) {
      for (Path versionFile : versionFiles) {
        String currentSchema = Files.readString(versionFile, StandardCharsets.UTF_8);
        long currentFingerprint = SchemaUtils.fingerprint(currentSchema);
        if (currentFingerprint == fingerprint) {
          int version = extractVersion(versionFile);
          return toRegistration(subjectDir, version, currentSchema);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read directory [" + subjectDir + "]", e);
    }

    throw new NotRegisteredException("Schema not registered for subject [" + subjectName + "]");
  }
}
