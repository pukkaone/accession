package com.github.pukkaone.accession.schema.registry.repository;

import com.github.pukkaone.accession.schema.registry.config.JGitSchemaRegistryProperties;
import com.github.pukkaone.accession.schema.registry.domain.ReadOnlySchemaRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.cloud.config.server.environment.JGitEnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Repository;

/**
 * Reads subjects and versions from Git repository.
 */
@Repository
public class JGitSchemaRegistryRepository {

  // Used for Git implementation, not to actually read an Environment.
  private JGitEnvironmentRepository delegate;

  /**
   * Constructor.
   *
   * @param environment
   *     environment
   * @param properties
   *     Git configuration properties
   */
  public JGitSchemaRegistryRepository(
      ConfigurableEnvironment environment, JGitSchemaRegistryProperties properties) {

    delegate = new JGitEnvironmentRepository(environment, properties);
  }

  private Path findSubjectsDirectory() {
    Path baseDir = delegate.getBasedir().toPath();
    for (String searchPath : delegate.getSearchPaths()) {
      String suffix = (searchPath.equals("/")) ? "" : searchPath;
      Path subjectsDir = baseDir.resolve(suffix);
      if (Files.isDirectory(subjectsDir)) {
        return subjectsDir;
      }
    }

    throw new IllegalStateException("None of searchPaths is a directory");
  }

  /**
   * Reads all subjects and schemas.
   *
   * @return subjects and schemas
   */
  public ReadOnlySchemaRegistry getSchemaRegistry() {
    delegate.refresh(delegate.getDefaultLabel());
    return new ReadOnlySchemaRegistry(findSubjectsDirectory());
  }
}
