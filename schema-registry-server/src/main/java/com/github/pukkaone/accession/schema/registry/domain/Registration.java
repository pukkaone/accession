package com.github.pukkaone.accession.schema.registry.domain;

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
@NoArgsConstructor
public class Registration {

  private int schemaId;
  private String subjectName;
  private int version;
  private String schema;
}
