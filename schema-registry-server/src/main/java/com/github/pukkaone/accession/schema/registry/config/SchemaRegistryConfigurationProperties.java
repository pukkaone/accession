package com.github.pukkaone.accession.schema.registry.config;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Schema registry configuration properties.
 */
@Component
@ConfigurationProperties("schema-registry")
@Data
@Validated
public class SchemaRegistryConfigurationProperties {

  @NotNull
  private String repositoryUri;

  @NotNull
  private String branch;

  private String subjectsDirectory = ".";

  private int refreshSeconds = 600;
}
