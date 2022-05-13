package com.github.pukkaone.accession.schema.registry.repository;

import com.github.pukkaone.accession.schema.registry.config.SchemaRegistryConfigurationProperties;
import com.github.pukkaone.accession.schema.registry.domain.Catalog;
import com.github.pukkaone.accession.schema.registry.domain.Registration;
import com.github.pukkaone.accession.schema.registry.function.SchemaUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.util.FileUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * Reads subjects and schemas from Git repository.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CatalogRepository {

  private static final char ID_SEPARATOR = '_';

  private final SchemaRegistryConfigurationProperties properties;

  private Path workingDir;
  private Git git;
  private ObjectId lastCommitId;

  private static int extractId(Path entry) {
    String name = entry.getFileName().toString();
    int idEnd = name.indexOf(ID_SEPARATOR);
    if (idEnd < 0) {
      return -1;
    }

    try {
      return Integer.parseInt(name.substring(0, idEnd));
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

  private static boolean isValidSubject(Path subjectDir) {
    return Files.isDirectory(subjectDir) &&
        !subjectDir.getFileName().toString().startsWith(".");
  }

  private Path createWorkingDir() {
    try {
      final Path tempDir = Files.createTempDirectory("schema-repository-");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          FileUtils.delete(tempDir.toFile(), FileUtils.RECURSIVE);
        } catch (IOException e) {
          log.warn("Failed to delete temporary directory {}", tempDir, e);
        }
      }));
      return tempDir;
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create temporary directory", e);
    }
  }

  private ObjectId resolveHead() {
    try {
      return git.getRepository().resolve("HEAD");
    } catch (IOException e) {
      throw new IllegalStateException("Cannot resolve HEAD", e);
    }
  }

  @PostConstruct
  private void initialize() {
    workingDir = createWorkingDir();
    try {
      log.info(
          "Cloning Git repository {} branch {}",
          properties.getRepositoryUri(),
          properties.getBranch());
      git = Git.cloneRepository()
          .setURI(properties.getRepositoryUri())
          .setBranch(properties.getBranch())
          .setDirectory(workingDir.toFile())
          .call();
    } catch (GitAPIException e) {
      throw new IllegalStateException(
          "Cannot clone Git repository " + properties.getRepositoryUri() +
              " branch " + properties.getBranch(),
          e);
    }

    lastCommitId = resolveHead();
    log.info("Cloned commit {}", lastCommitId.getName());
  }

  /**
   * Pulls changes from remote repository.
   *
   * @return true if changes found
   */
  @SuppressWarnings("checkstyle:IllegalCatch")
  public boolean pull() {
    try {
      git.pull().call();

      ObjectId commitId = resolveHead();
      if (commitId.equals(lastCommitId)) {
        log.debug("Pulled unchanged commit {}", commitId);
        return false;
      }

      lastCommitId = commitId;
      log.info("Pulled commit {}", lastCommitId.getName());
      return true;
    } catch (Exception e) {
      log.error("Cannot pull", e);
      return false;
    }
  }

  private List<Path> listVersionFiles(Path subjectDir) {
    log.info("Loading directory {}", subjectDir);

    DirectoryStream<Path> versionFiles;
    try {
      versionFiles = Files.newDirectoryStream(
          subjectDir,
          entry -> Files.isRegularFile(entry));
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read directory " + subjectDir, e);
    }

    List<Path> sortedFiles = new ArrayList<>();
    for (Path versionFile : versionFiles) {
      if (extractId(versionFile) >= 0) {
        sortedFiles.add(versionFile);
      } else {
        log.warn("Ignoring invalid file name [{}]", versionFile);
      }
    }

    sortedFiles.sort(Comparator.comparingInt(CatalogRepository::extractId));
    return sortedFiles;
  }

  private String readVersion(Path versionFile) {
    log.info("Loading file {}", versionFile);
    try {
      byte[] schemaBytes = Files.readAllBytes(versionFile);
      return new String(schemaBytes, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read file " + versionFile, e);
    }
  }

  private void readSubject(Path subjectDir, Catalog catalog) {
    int subjectId = extractId(subjectDir);
    String subjectName = extractName(subjectDir);
    if (subjectId < 0 || StringUtils.isEmpty(subjectName)) {
      log.warn("Ignoring invalid directory name [{}]", subjectDir);
      return;
    }

    Map<Long, Registration> fingerprintToRegistrationMap = new HashMap<>();
    TreeMap<Integer, Registration> versionToRegistrationMap = new TreeMap<>();
    List<Path> versionFiles = listVersionFiles(subjectDir);
    String latestSchema = null;
    Path latestFile = null;
    for (Path versionFile : versionFiles) {
      String schema = readVersion(versionFile);
      if (latestSchema != null && !SchemaUtils.isCompatible(schema, latestSchema)) {
        log.warn("Schema {} is incompatible with latest schema {}", versionFile, latestFile);
      }
      latestSchema = schema;
      latestFile = versionFile;

      long fingerprint = SchemaUtils.fingerprint(schema);
      int version = extractId(versionFile);
      int schemaId = subjectId * 10000 + version;

      Registration registration = Registration.builder()
          .schemaId(schemaId)
          .subject(subjectName)
          .version(version)
          .schema(schema)
          .build();
      fingerprintToRegistrationMap.put(fingerprint, registration);
      versionToRegistrationMap.put(version, registration);
      catalog.getIdToRegistrationMap().put(schemaId, registration);
    }

    catalog.getSubjectToFingerprintToRegistrationMap()
        .put(subjectName, fingerprintToRegistrationMap);
    catalog.getSubjectToVersionToRegistrationMap()
        .put(subjectName, versionToRegistrationMap);
  }

  /**
   * Reads all subjects and schemas.
   *
   * @return subjects and schemas
   */
  public Catalog readCatalog() {
    Catalog catalog = new Catalog();
    try {
      DirectoryStream<Path> subjectDirs = Files.newDirectoryStream(
          workingDir.resolve(properties.getSubjectsDirectory()),
          CatalogRepository::isValidSubject);

      for (Path subjectDir : subjectDirs) {
        readSubject(subjectDir, catalog);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read subjects", e);
    }

    return catalog;
  }
}
