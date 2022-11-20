package com.github.pukkaone.accession.schema.registry;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.eclipse.jgit.api.ResetCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileSystemUtils;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PluginTest {

  @RegisterExtension
  GitRepository gitRepository = new GitRepository();

  private static final Path BASE_DIR = Path.of(System.getProperty("user.dir"));
  private static final BasicJsonTester JSON = new BasicJsonTester(PluginTest.class);

  private InvocationResult maven(
      List<String> goals, Map<String, ?> propertyNameToValueMap) throws Exception {

    InvocationRequest request = new DefaultInvocationRequest();
    request.setBaseDirectory(BASE_DIR.toFile());
    request.setBatchMode(true);
    request.setGoals(goals);
    request.setShowErrors(true);
    var properties = new Properties();
    properties.put("accession.version", System.getProperty("accession.version", "999-SNAPSHOT"));
    properties.put("registryUri", gitRepository.getWorkTree().toString());
    properties.putAll(propertyNameToValueMap);
    request.setProperties(properties);

    Invoker invoker = new DefaultInvoker();
    invoker.setMavenHome(new File("."));
    invoker.setMavenExecutable(new File("../mvnw"));

    return invoker.execute(request);
  }

  @BeforeEach
  void beforeEach() throws Exception {
    FileSystemUtils.deleteRecursively(Path.of("target/schema-registry"));
  }

  @Test
  void when_generate_then_success() throws Exception {
    InvocationResult result = maven(
        List.of("compile", "schema-registry:generate"),
        Map.of());
    assertThat(result.getExitCode()).isEqualTo(0);

    var avscFile = BASE_DIR.resolve("target/topic.avsc").toFile();
    var expectedAvscFile = new ClassPathResource("expected-001.avsc").getInputStream();
    assertThat(JSON.from(avscFile)).isEqualToJson(expectedAvscFile);
  }

  @Order(1)
  @Test
  void when_register_then_success() throws Exception {
    InvocationResult result = maven(
        List.of("compile", "schema-registry:register"),
        Map.of());
    assertThat(result.getExitCode()).isEqualTo(0);

    gitRepository.getGit()
        .reset()
        .setMode(ResetCommand.ResetType.HARD)
        .setRef("HEAD")
        .call();
    var avscFile = gitRepository.getWorkTree().resolve("3037340_topic-value/001.avsc").toFile();
    var expectedAvscFile = new ClassPathResource("expected-001.avsc").getInputStream();
    assertThat(JSON.from(avscFile)).isEqualToJson(expectedAvscFile);
  }

  @Order(2)
  @Test
  void given_incompatible_schema_when_register_then_error() throws Exception {
    InvocationResult result = maven(
        List.of("compile", "schema-registry:register"),
        Map.of("registrationVersion", "2",
            "reflectClass", "com.example.IncompatibleEvent"));
    assertThat(result.getExitCode()).isEqualTo(1);
  }

  @Order(3)
  @Test
  void given_compatible_schema_when_register_then_success() throws Exception {
    InvocationResult result = maven(
        List.of("compile", "schema-registry:register"),
        Map.of("registrationVersion", "2",
            "reflectClass", "com.example.CompatibleEvent"));
    assertThat(result.getExitCode()).isEqualTo(0);

    gitRepository.getGit()
        .reset()
        .setMode(ResetCommand.ResetType.HARD)
        .setRef("HEAD")
        .call();
    var avscFile = gitRepository.getWorkTree().resolve("3037340_topic-value/002.avsc").toFile();
    var expectedAvscFile = new ClassPathResource("expected-002.avsc").getInputStream();
    assertThat(JSON.from(avscFile)).isEqualToJson(expectedAvscFile);
  }

  @Order(4)
  @Test
  void given_mutuable_versions_false_when_register_identical_schema_then_success() throws Exception {
    InvocationResult result = maven(
        List.of("compile", "schema-registry:register"),
        Map.of("registrationVersion", "2",
            "reflectClass", "com.example.CompatibleEvent"));
    assertThat(result.getExitCode()).isEqualTo(0);
  }

  @Order(4)
  @Test
  void given_mutuable_versions_false_when_register_different_schema_then_error() throws Exception {
    InvocationResult result = maven(
        List.of("compile", "schema-registry:register"),
        Map.of("registrationVersion", "2",
            "reflectClass", "com.example.Event"));
    assertThat(result.getExitCode()).isEqualTo(1);
  }

  @Order(5)
  @Test
  void given_mutuable_versions_true_when_register_different_schema_then_success() throws Exception {
    InvocationResult result = maven(
        List.of("compile", "schema-registry:register"),
        Map.of("mutableVersions", "true",
            "registrationVersion", "2",
            "reflectClass", "com.example.Event"));
    assertThat(result.getExitCode()).isEqualTo(0);
  }
}
