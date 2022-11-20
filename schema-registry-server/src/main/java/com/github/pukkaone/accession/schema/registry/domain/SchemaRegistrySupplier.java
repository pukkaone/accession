package com.github.pukkaone.accession.schema.registry.domain;

import com.github.pukkaone.accession.schema.registry.config.SchemaRegistryProperties;
import com.github.pukkaone.accession.schema.registry.repository.GitRepository;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Supplies schema registry that reads from Git repository.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaRegistrySupplier {

  private final SchemaRegistryProperties properties;
  private final GitRepository gitRepository;

  private String headId;
  private ReadOnlySchemaRegistry schemaRegistry;

  /**
   * Supplies schema registry.
   *
   * @return schema registry
   */
  public synchronized ReadOnlySchemaRegistry getSchemaRegistry() {
    String headId = gitRepository.refresh();
    if (schemaRegistry == null || !headId.equals(this.headId)) {
      log.info("Pulled commit_id={}", headId);
      this.headId = headId;
      Path subjectsDir = gitRepository.getWorkingDir().resolve(properties.getSubjectsDirectory());
      schemaRegistry = new ReadOnlySchemaRegistry(subjectsDir);
    }

    return schemaRegistry;
  }
}
