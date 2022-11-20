package com.github.pukkaone.accession.schema.registry.repository;

import com.github.pukkaone.accession.schema.registry.config.SchemaRegistryProperties;
import com.github.pukkaone.accession.schema.registry.ssh.PropertyBasedSshSessionFactory;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.util.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Clones a Git repository.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GitRepository {

  private final SchemaRegistryProperties properties;

  @Getter
  private Path workingDir;

  private Git git;

  private Path createTempDirectory() {
    try {
      Path tempDir = Files.createTempDirectory("schema-repository-");
      tempDir.toFile().deleteOnExit();
      return tempDir;
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create temporary directory", e);
    }
  }

  private void cloneRepository() {
    try {
      log.info("Cloning Git repository={} label={}", properties.getUri(), properties.getLabel());
      git = Git.cloneRepository()
          .setURI(properties.getUri())
          .setBranch(properties.getLabel())
          .setDirectory(workingDir.toFile())
          .call();
    } catch (GitAPIException | JGitInternalException e) {
      throw new IllegalStateException(
          "Cannot clone Git repository " + properties.getUri() + " label " + properties.getLabel(),
          e);
    }
  }

  @PostConstruct
  private void initialize() {
    workingDir = createTempDirectory();

    if (StringUtils.hasText(properties.getPrivateKey())) {
      SshSessionFactory.setInstance(new PropertyBasedSshSessionFactory(properties));
    }

    cloneRepository();
  }

  private boolean pull() {
    try {
      git.pull().call();
      return true;
    } catch (GitAPIException | JGitInternalException e) {
      log.warn("Cannot pull", e);
    }

    return false;
  }

  private void createWorkingDir() {
    try {
      Files.createDirectory(workingDir);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create directory " + workingDir, e);
    }
  }

  private String resolveHead() {
    try {
      return git.getRepository()
          .resolve(Constants.HEAD)
          .getName();
    } catch (IOException e) {
      throw new IllegalStateException("Cannot resolve HEAD", e);
    }
  }

  /**
   * Pulls changes from remote repository.
   *
   * @return head commit ID
   */
  public String refresh() {
    if (!pull()) {
      // Clear working directory and clone repository again.
      try {
        FileUtils.delete(workingDir.toFile(), FileUtils.RECURSIVE);
      } catch (IOException e) {
        throw new IllegalStateException("Cannot delete directory " + workingDir, e);
      } finally {
        createWorkingDir();
      }

      cloneRepository();
    }

    return resolveHead();
  }
}
