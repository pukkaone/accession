package com.github.pukkaone.accession.schema.registry.maven.goal;

import com.github.pukkaone.accession.schema.registry.maven.Registration;
import com.github.pukkaone.accession.schema.registry.maven.SchemaBuilder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Generates Avro schema.
 */
@Mojo(
    name = "generate",
    defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES,
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true)
public class GenerateMojo extends AbstractMojo {

  /**
   * Schema registration requests.
   */
  @Parameter(property = "schema-registry.registrations", required = true)
  private List<Registration> registrations;

  /**
   * If true, then do not execute plugin.
   */
  @Parameter(property = "schema-registry.skip", defaultValue = "false")
  private boolean skip;

  @Parameter(property = "project", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      getLog().info("schema-registry.skip is " + skip);
      return;
    }

    SchemaBuilder schemaBuilder;
    try {
      schemaBuilder = new SchemaBuilder(project.getTestClasspathElements());
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoExecutionException("Cannot get test classpath", e);
    }

    for (Registration registration : registrations) {
      Path schemaFile = Paths.get(
          project.getBuild().getDirectory(), registration.getSubject() + ".avsc");
      getLog().info(String.format("Generating schema file %s", schemaFile));
      Schema schema = schemaBuilder.generateSchema(registration);
      SchemaBuilder.writeSchemaFile(schema, schemaFile);
    }
  }
}
