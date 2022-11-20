package com.github.pukkaone.accession.schema.registry.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body returned when error occurred.
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class Error {

  @JsonProperty("error_code")
  private int errorCode;

  @JsonProperty("message")
  private String message;
}
