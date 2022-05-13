package com.github.pukkaone.accession.schema.registry.maven;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;

/**
 * Accesses schema registry.
 */
public class SchemaRegistryService implements Closeable {

  private CompatibilityDirection compatibilityDirection;
  private String projectBranch;
  private RegistryRepository registryRepository;
  private Path workingDir;
  private Git git;
  private SchemaBuilder schemaBuilder;

  /**
   * Constructor.
   *
   * @param compatibilityDirection
   *         compatilibity mode
   * @param registryRepositories
   *         project branch to schema registry Git repository mappings
   * @param projectBaseDir
   *         project base directory
   * @param workingDirParent
   *         parent directory of cloned schema registry Git repository working directory
   * @param schemaBuilder
   *         schema builder
   */
  public SchemaRegistryService(
      CompatibilityDirection compatibilityDirection,
      List<RegistryRepository> registryRepositories,
      File projectBaseDir,
      String workingDirParent,
      SchemaBuilder schemaBuilder) {

    this.compatibilityDirection = compatibilityDirection;
    this.projectBranch = findProjectBranch(projectBaseDir);
    registryRepository = resolveToRegistryRepository(registryRepositories);
    cloneRegistryRepository(workingDirParent);
    this.schemaBuilder = schemaBuilder;
  }

  /**
   * Gets project branch.
   *
   * @return project branch
   */
  public String getProjectBranch() {
    return projectBranch;
  }

  /**
   * Gets schema registry branch.
   *
   * @return registry branch
   */
  public String getRegistryBranch() {
    return registryRepository.getRegistryBranch();
  }

  private static String getFullBranch(File workingDir) throws IOException {
    try (Git git = Git.open(workingDir)) {
      return git.getRepository().getFullBranch();
    }
  }

  private static String findProjectBranch(File projectBaseDir) {
    String projectBranch = System.getenv().get("GIT_BRANCH");
    if (projectBranch != null) {
      return projectBranch;
    }

    projectBranch = System.getenv().get("BRANCH_NAME");
    if (projectBranch != null) {
      return projectBranch;
    }

    File workingDir = projectBaseDir;
    while (workingDir != null) {
      try {
        return getFullBranch(workingDir);
      } catch (IOException e) {
        workingDir = projectBaseDir.getParentFile();
      }
    }

    throw new IllegalStateException(
        "Cannot open project local Git repository " + projectBaseDir);
  }

  private RegistryRepository resolveToRegistryRepository(
      List<RegistryRepository> registryRepositories) {

    if (registryRepositories.isEmpty()) {
      throw new IllegalArgumentException("No registryRepository entries are configured");
    }

    for (RegistryRepository registryRepository : registryRepositories) {
      if (projectBranch.matches(registryRepository.getProjectBranchPattern())) {
        return registryRepository;
      }
    }

    throw new IllegalArgumentException("Cannot resolve project branch " + projectBranch);
  }

  private void cloneRegistryRepository(String workingDirParent) {
    workingDir = Paths.get(workingDirParent, "schema-registry");
    if (Files.exists(workingDir)) {
      try {
        git = Git.open(workingDir.toFile());
      } catch (IOException e) {
        throw new IllegalStateException(
            "Cannot open Git working directory " + workingDir, e);
      }

      try {
        git.pull()
            .call();
      } catch (GitAPIException e) {
        throw new IllegalStateException("Cannot pull", e);
      }
    } else {
      try {
        Files.createDirectory(workingDir);
      } catch (IOException e) {
        throw new IllegalStateException("Cannot create directory " + workingDir, e);
      }

      try {
        git = Git.cloneRepository()
            .setURI(registryRepository.getRegistryUri())
            .setBranch(registryRepository.getRegistryBranch())
            .setDirectory(workingDir.toFile())
            .call();
      } catch (GitAPIException e) {
        throw new IllegalStateException(
            "Cannot clone Git repository " + registryRepository.getRegistryUri() +
                " branch " + registryRepository.getRegistryBranch(),
            e);
      }
    }
  }

  private void commit(VersionAction action, Registration registration) {
    try {
      git.add()
          .addFilepattern(".")
          .call();
    } catch (GitAPIException e) {
      throw new IllegalStateException("Cannot add files", e);
    }

    try {
      git.commit()
          .setMessage(String.format(
              "%s %s version %d",
              action.getMessage(),
              registration.getSubject(),
              registration.getVersion()))
          .call();
    } catch (GitAPIException e) {
      throw new IllegalStateException("Cannot commit", e);
    }
  }

  /**
   * Puts schema in schema registry.
   *
   * @param registration
   *     registration request
   * @return true if registry changed
   */
  public boolean register(Registration registration) {
    SchemaRegistry schemaRegistry = new SchemaRegistry(workingDir, schemaBuilder);
    VersionAction action = schemaRegistry.register(
        registration, compatibilityDirection, registryRepository.isMutableVersions());
    if (action != VersionAction.NONE) {
      commit(action, registration);
    }

    return action != VersionAction.NONE;
  }

  /**
   * Pushes changes to remote registry Git repository.
   *
   * @return push result messages
   */
  public String pushToOrigin() {
    try {
      Iterable<PushResult> pushResults = git.push()
          .add(registryRepository.getRegistryBranch())
          .call();
      return StreamSupport.stream(pushResults.spliterator(), false)
          .map(PushResult::getMessages)
          .collect(Collectors.joining("\n"));
    } catch (GitAPIException e) {
      throw new IllegalStateException("Cannot push", e);
    }
  }

  @Override
  public void close() throws IOException {
    git.close();
  }
}
