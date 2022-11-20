package com.github.pukkaone.accession.schema.registry.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Record of schema version in registry.
 */
@AllArgsConstructor
@Builder
@Data
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
public class SchemaResponse {

  private Integer id;
  private String name;
  private String subject;
  private Integer version;
  private String schema;
}
