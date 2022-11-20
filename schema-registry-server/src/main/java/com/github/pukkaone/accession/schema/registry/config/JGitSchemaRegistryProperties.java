package com.github.pukkaone.accession.schema.registry.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.server.environment.JGitEnvironmentProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Schema registry server configuration properties. Extends {@link JGitEnvironmentProperties} to
 * reuse its implemenation, but will not actually be used to read an Environment.
 */
@Component
@ConfigurationProperties("schema.registry.server.git")
@Validated
public class JGitSchemaRegistryProperties extends JGitEnvironmentProperties {
}
