package com.github.pukkaone.accession.schema.registry.goal;

import com.github.pukkaone.accession.schema.registry.CompatibilityDirection;
import com.github.pukkaone.accession.schema.registry.SchemaBuilder;
import com.github.pukkaone.accession.schema.registry.SchemaRegistryService;
import com.github.pukkaone.accession.schema.registry.configuration.Registration;
import com.github.pukkaone.accession.schema.registry.configuration.RegistryRepository;
import java.io.IOException;
import java.util.List;
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
 * Puts schema in schema registry.
 */
@Mojo(
    name = "register",
    defaultPhase = LifecyclePhase.DEPLOY,
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true)
public class RegisterMojo extends AbstractMojo {

  /**
   * How to compare new schema to registered schema for compatibility. Allowed values are BACKWARD,
   * FORWARD, FULL.
   */
  @Parameter(property = "schema-registry.compatibility", defaultValue = "FORWARD")
  private CompatibilityDirection compatibility;

  /**
   * Mappings from project Git branch to schema registry Git repository. The first matching mapping
   * takes effect.
   */
  @Parameter(property = "schema-registry.registryRepositories", required = true)
  private List<RegistryRepository> registryRepositories;

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

    SchemaRegistryService service = new SchemaRegistryService(
        compatibility,
        registryRepositories,
        project.getBasedir(),
        project.getBuild().getDirectory(),
        schemaBuilder);
    getLog().info("Project Git branch " + service.getProjectBranch());
    getLog().info("Registry Git branch " + service.getRegistryBranch());

    boolean changed = false;
    for (Registration registration : registrations) {
      getLog().info(String.format(
          "Registering subject %s, version %d, reflectClass %s",
          registration.getSubject(),
          registration.getVersion(),
          registration.getReflectClass()));
      if (service.register(registration)) {
        getLog().info("Committed schema change");
        changed = true;
      }
    }

    if (changed) {
      String pushResultMessages = service.pushToOrigin();
      getLog().info(pushResultMessages);
    }

    try {
      service.close();
    } catch (IOException e) {
      throw new MojoExecutionException("Cannot close Git repository", e);
    }
  }
}
