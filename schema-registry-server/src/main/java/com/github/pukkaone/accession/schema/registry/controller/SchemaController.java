package com.github.pukkaone.accession.schema.registry.controller;

import com.github.pukkaone.accession.schema.registry.dto.SchemaResponse;
import com.github.pukkaone.accession.schema.registry.repository.JGitSchemaRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Schema endpoint.
 */
@RequestMapping(
    value = SchemaController.BASE_PATH,
    produces = MediaTypes.APPLICATION_SCHEMA_REGISTRY)
@RequiredArgsConstructor
@RestController
public class SchemaController {

  public static final String BASE_PATH = "/schemas";
  public static final String GET_SCHEMA_BY_SCHEMA_ID_PATH = "/ids/{schemaId}";

  private final JGitSchemaRegistryRepository schemaRegistryRepository;

  /**
   * Gets schema by schema ID.
   *
   * @param schemaId
   *     schema ID to get
   * @return schema
   */
  @GetMapping(GET_SCHEMA_BY_SCHEMA_ID_PATH)
  public SchemaResponse getSchemaBySchemaId(@PathVariable int schemaId) {
    return schemaRegistryRepository.getSchemaRegistry()
        .findSchemaBySchemaId(schemaId);
  }
}
