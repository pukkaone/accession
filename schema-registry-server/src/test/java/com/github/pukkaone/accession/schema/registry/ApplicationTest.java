package com.github.pukkaone.accession.schema.registry;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.pukkaone.accession.schema.registry.repository.JGitSchemaRegistryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;

/**
 * Application test.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ApplicationTest {

  @MockBean
  private JGitSchemaRegistryRepository schemaRegistryRepository;

  @Autowired
  private Environment environment;

  @Test
  void should_start() {
    assertThat(environment.getActiveProfiles()).isEmpty();
  }
}
