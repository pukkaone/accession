package com.github.pukkaone.accession.schema.registry.controller;

import com.github.pukkaone.accession.schema.registry.dto.SchemaResponse;
import com.github.pukkaone.accession.schema.registry.repository.JGitSchemaRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
public class SchemaController {

  private final JGitSchemaRegistryRepository schemaRegistryRepository;

  /**
   * Gets schema by schema ID.
   *
   * @param schemaId
   *     schema ID to get
   * @return schema
   */
  @RequestMapping(value = "/ids/{schemaId}", method = RequestMethod.GET)
  public SchemaResponse getSchemaBySchemaId(@PathVariable("schemaId") int schemaId) {
    return schemaRegistryRepository.getSchemaRegistry()
        .findSchemaBySchemaId(schemaId);
  }
}
