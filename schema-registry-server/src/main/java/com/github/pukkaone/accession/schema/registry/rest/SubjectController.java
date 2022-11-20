package com.github.pukkaone.accession.schema.registry.rest;

import com.github.pukkaone.accession.schema.registry.domain.Registration;
import com.github.pukkaone.accession.schema.registry.rest.dto.Error;
import com.github.pukkaone.accession.schema.registry.rest.dto.SchemaRequest;
import com.github.pukkaone.accession.schema.registry.rest.dto.SchemaResponse;
import com.github.pukkaone.accession.schema.registry.service.SchemaService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  private static final int NOT_REGISTERED_ERROR_CODE = 42202;

  private final SchemaService schemaService;

  /**
   * Gets all registered subjects.
   *
   * @return subjects
   */
  @RequestMapping(method = RequestMethod.GET)
  public Set<String> getSubjects() {
    return schemaService.getSubjects();
  }

  /**
   * Checks if a schema has already been registered under the subject.
   *
   * @param subject
   *     subject to search by
   * @param schemaRequest
   *     schema to search by
   * @return schema ID
   */
  @RequestMapping(value = "/{subject}", method = RequestMethod.POST)
  public ResponseEntity<?> getRegistration(
      @PathVariable("subject") String subject, @RequestBody SchemaRequest schemaRequest) {

    Registration registration = schemaService.findBySubjectAndSchema(
        subject, schemaRequest.getSchema());
    if (registration == null) {
      Error error = Error.builder()
          .errorCode(NOT_REGISTERED_ERROR_CODE)
          .message(String.format("Schema not registered for subject %s", subject))
          .build();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    SchemaResponse schemaResponse = SchemaResponse.builder()
        .id(registration.getSchemaId())
        .subject(registration.getSubject())
        .version(registration.getVersion())
        .schema(registration.getSchema())
        .build();
    return ResponseEntity.ok(schemaResponse);
  }

  /**
   * Gets versions by subject.
   *
   * @param subject
   *     subject to search by
   * @return versions
   */
  @RequestMapping(value = "/{subject}/versions", method = RequestMethod.GET)
  public Set<Integer> getVersions(@PathVariable("subject") String subject) {
    return schemaService.findBySubject(subject);
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
  public ResponseEntity<?> getSchemaId(
      @PathVariable("subject") String subject, @RequestBody SchemaRequest schemaRequest) {

    Registration registration = schemaService.findBySubjectAndSchema(
        subject, schemaRequest.getSchema());
    if (registration == null) {
      Error error = Error.builder()
          .errorCode(NOT_REGISTERED_ERROR_CODE)
          .message(String.format("Schema not registered for subject %s", subject))
          .build();
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    SchemaResponse schemaResponse = SchemaResponse.builder()
        .id(registration.getSchemaId())
        .build();
    return ResponseEntity.ok(schemaResponse);
  }

  /**
   * Gets registration by subject and version.
   *
   * @param subject
   *     subject to search by
   * @param version
   *     version to search by
   * @return schema
   */
  @RequestMapping(value = "/{subject}/versions/{version}", method = RequestMethod.GET)
  public ResponseEntity<?> getVersion(
      @PathVariable("subject") String subject,
      @PathVariable("version") String version) {

    SchemaResponse schemaResponse = ("latest".equals(version))
        ? schemaService.findLatestSchemaBySubject(subject)
        : schemaService.findBySubjectAndVersion(subject, Integer.parseInt(version));
    if (schemaResponse == null) {
      Error error = Error.builder()
          .errorCode(NOT_REGISTERED_ERROR_CODE)
          .message(String.format("Schema not registered for subject %s", subject))
          .build();
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    return ResponseEntity.ok(schemaResponse);
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
  public ResponseEntity<?> getSchema(
      @PathVariable("subject") String subject,
      @PathVariable("version") String version) {

    SchemaResponse schemaResponse = ("latest".equals(version))
        ? schemaService.findLatestSchemaBySubject(subject)
        : schemaService.findBySubjectAndVersion(subject, Integer.parseInt(version));
    if (schemaResponse == null) {
      Error error = Error.builder()
          .errorCode(NOT_REGISTERED_ERROR_CODE)
          .message(String.format("Schema not registered for subject %s", subject))
          .build();
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    return ResponseEntity.ok(schemaResponse.getSchema());
  }
}
