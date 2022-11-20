package com.github.pukkaone.accession.schema.registry.controller;

import com.github.pukkaone.accession.schema.registry.domain.ReadOnlySchemaRegistry;
import com.github.pukkaone.accession.schema.registry.domain.Registration;
import com.github.pukkaone.accession.schema.registry.dto.SchemaRequest;
import com.github.pukkaone.accession.schema.registry.dto.SchemaResponse;
import com.github.pukkaone.accession.schema.registry.repository.JGitSchemaRegistryRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Subject endpoint.
 */
@RequestMapping(
    value = "/subjects",
    produces = {MediaTypes.APPLICATION_SCHEMA_REGISTRY, MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@RestController
public class SubjectController {

  private final JGitSchemaRegistryRepository schemaRegistryRepository;

  private ReadOnlySchemaRegistry getSchemaRegistry() {
    return schemaRegistryRepository.getSchemaRegistry();
  }

  /**
   * Gets all subjects.
   *
   * @return subjects
   */
  @RequestMapping(method = RequestMethod.GET)
  public Set<String> getSubjects() {
    return getSchemaRegistry().findSubjectNames();
  }

  /**
   * Checks if a schema is already registered under the subject.
   *
   * @param subject
   *     subject to search by
   * @param schemaRequest
   *     schema to search by
   * @return schema ID
   */
  @RequestMapping(value = "/{subject}", method = RequestMethod.POST)
  public SchemaResponse getRegistration(
      @PathVariable("subject") String subject,
      @RequestBody SchemaRequest schemaRequest) {

    Registration registration = getSchemaRegistry().findRegistrationBySubjectNameAndSchema(
        subject, schemaRequest.getSchema());
    return SchemaResponse.builder()
        .id(registration.getSchemaId())
        .subject(registration.getSubjectName())
        .version(registration.getVersion())
        .schema(registration.getSchema())
        .build();
  }

  /**
   * Gets versions by subject.
   *
   * @param subject
   *     subject to search by
   * @return versions
   */
  @RequestMapping(value = "/{subject}/versions", method = RequestMethod.GET)
  public List<Integer> getVersions(@PathVariable("subject") String subject) {
    return getSchemaRegistry().findVersionsBySubjectName(subject);
  }

  /**
   * Gets schema ID by schema.
   *
   * @param subject
   *     subject to search by
   * @param schemaRequest
   *     schema to search by
   * @return schema ID
   */
  @RequestMapping(value = "/{subject}/versions", method = RequestMethod.POST)
  public SchemaResponse getSchemaId(
      @PathVariable("subject") String subject,
      @RequestBody SchemaRequest schemaRequest) {

    Registration registration = getSchemaRegistry().findRegistrationBySubjectNameAndSchema(
        subject, schemaRequest.getSchema());
    return SchemaResponse.builder()
        .id(registration.getSchemaId())
        .build();
  }

  /**
   * Gets registration by subject and version.
   *
   * @param subject
   *     subject to search by
   * @param version
   *     version to search by
   * @return registration
   */
  @RequestMapping(value = "/{subject}/versions/{version}", method = RequestMethod.GET)
  public SchemaResponse getRegistrationBySubjectAndVersion(
      @PathVariable("subject") String subject,
      @PathVariable("version") String version) {

    return ("latest".equals(version))
        ? getSchemaRegistry().findLatestSchemaBySubjectName(subject)
        : getSchemaRegistry().findSchemaBySubjectNameAndVersion(subject, Integer.parseInt(version));
  }

  /**
   * Gets schema by subject and version.
   *
   * @param subject
   *     subject to search by
   * @param version
   *     version to search by
   * @return schema
   */
  @RequestMapping(value = "/{subject}/versions/{version}/schema", method = RequestMethod.GET)
  public String getSchemaBySubjectAndVersion(
      @PathVariable("subject") String subject,
      @PathVariable("version") String version) {

    SchemaResponse schemaResponse = getRegistrationBySubjectAndVersion(subject, version);
    return schemaResponse.getSchema();
  }
}
