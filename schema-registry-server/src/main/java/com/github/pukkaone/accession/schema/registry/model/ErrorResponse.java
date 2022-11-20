package com.github.pukkaone.accession.schema.registry.model;

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
public class ErrorResponse {

  @JsonProperty("error_code")
  private int errorCode;

  @JsonProperty("message")
  private String message;
}
