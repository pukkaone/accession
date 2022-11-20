package com.github.pukkaone.accession.schema.registry.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Schema registry server configuration properties.
 */
@Component
@ConfigurationProperties("schema.registry.server.git")
@Data
@Validated
public class SchemaRegistryProperties {

  private String hostKey;

  /**
   * One of ssh-dss, ssh-rsa, ssh-ed25519, ecdsa-sha2-nistp256, ecdsa-sha2-nistp384, or
   * ecdsa-sha2-nistp521.
   */
  private String hostKeyAlgorithm;

  @NotBlank
  private String label;

  private String privateKey;

  private String subjectsDirectory = ".";

  @NotBlank
  private String uri;
}
