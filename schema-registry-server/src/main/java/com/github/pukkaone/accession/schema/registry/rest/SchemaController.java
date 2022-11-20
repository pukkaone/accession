package com.github.pukkaone.accession.schema.registry.rest;

import com.github.pukkaone.accession.schema.registry.rest.dto.Error;
import com.github.pukkaone.accession.schema.registry.rest.dto.SchemaResponse;
import com.github.pukkaone.accession.schema.registry.service.SchemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Schema endpoint.
 */
@RequestMapping(
    value = "/schemas",
    produces = {MediaTypes.APPLICATION_SCHEMA_REGISTRY, MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@RestController
@Slf4j
public class SchemaController {

  private static final int NOT_FOUND_ERROR_CODE = 40403;

  private final SchemaService schemaService;

  /**
   * Gets schema by schema ID.
   *
   * @param schemaId
   *     schema ID to get
   * @return schema
   */
  @RequestMapping(value = "/ids/{schemaId}", method = RequestMethod.GET)
  public ResponseEntity<?> register(@PathVariable("schemaId") int schemaId) {
    SchemaResponse schemaResponse = schemaService.findBySchemaId(schemaId);
    if (schemaResponse == null) {
      Error error = Error.builder()
          .errorCode(NOT_FOUND_ERROR_CODE)
          .message(String.format("Schema ID %s not found", schemaId))
          .build();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    return ResponseEntity.ok(schemaResponse);
  }
}
