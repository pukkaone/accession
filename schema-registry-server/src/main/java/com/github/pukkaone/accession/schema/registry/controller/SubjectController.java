package com.github.pukkaone.accession.schema.registry.controller;

import com.github.pukkaone.accession.schema.registry.domain.ReadOnlySchemaRegistry;
import com.github.pukkaone.accession.schema.registry.domain.Registration;
import com.github.pukkaone.accession.schema.registry.domain.SchemaRegistrySupplier;
import com.github.pukkaone.accession.schema.registry.model.SchemaRequest;
import com.github.pukkaone.accession.schema.registry.model.SchemaResponse;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Subject endpoint.
 */
@RequestMapping(
    value = SubjectController.BASE_PATH,
    produces = MediaTypes.APPLICATION_SCHEMA_REGISTRY)
@RequiredArgsConstructor
@RestController
public class SubjectController {

  public static final String BASE_PATH = "/subjects";
  public static final String GET_REGISTRATION_BY_SUBJECT_AND_SCHEMA_PATH = "/{subject}";
  public static final String GET_SCHEMA_ID_BY_SUBJECT_AND_SCHEMA_PATH = "/{subject}/versions";
  public static final String GET_REGISTRATION_BY_SUBJECT_AND_VERSION_PATH =
      "/{subject}/versions/{version}";
  public static final String GET_SCHEMA_BY_SUBJECT_AND_VERSION_PATH =
      "/{subject}/versions/{version}/schema";

  private final SchemaRegistrySupplier schemaRegistryRepository;

  private ReadOnlySchemaRegistry getSchemaRegistry() {
    return schemaRegistryRepository.getSchemaRegistry();
  }

  /**
   * Gets all subjects.
   *
   * @return subjects
   */
  @GetMapping
  public Set<String> getSubjects() {
    return getSchemaRegistry().findSubjectNames();
  }

  /**
   * Checks a schema is registered under a subject.
   *
   * @param subject
   *     subject to search by
   * @param schemaRequest
   *     schema to search by
   * @return registration
   */
  @PostMapping(GET_REGISTRATION_BY_SUBJECT_AND_SCHEMA_PATH)
  public SchemaResponse getRegistrationBySubjectAndSchema(
      @PathVariable String subject,
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
  @GetMapping(GET_SCHEMA_ID_BY_SUBJECT_AND_SCHEMA_PATH)
  public List<Integer> getVersionsBySubject(@PathVariable String subject) {
    return getSchemaRegistry().findVersionsBySubjectName(subject);
  }

  /**
   * Gets schema ID by subject and schema.
   *
   * @param subject
   *     subject to search by
   * @param schemaRequest
   *     schema to search by
   * @return schema ID
   */
  @PostMapping(GET_SCHEMA_ID_BY_SUBJECT_AND_SCHEMA_PATH)
  public SchemaResponse getSchemaIdBySubjectAndSchema(
      @PathVariable String subject,
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
  @GetMapping(GET_REGISTRATION_BY_SUBJECT_AND_VERSION_PATH)
  public SchemaResponse getRegistrationBySubjectAndVersion(
      @PathVariable String subject,
      @PathVariable String version) {

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
  @GetMapping(GET_SCHEMA_BY_SUBJECT_AND_VERSION_PATH)
  public ResponseEntity<String> getSchemaBySubjectAndVersion(
      @PathVariable String subject,
      @PathVariable String version) {

    SchemaResponse schemaResponse = getRegistrationBySubjectAndVersion(subject, version);
    return ResponseEntity.ok(schemaResponse.getSchema());
  }
}
