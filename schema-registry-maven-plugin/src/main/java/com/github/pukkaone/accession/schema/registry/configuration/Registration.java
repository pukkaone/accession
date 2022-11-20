package com.github.pukkaone.accession.schema.registry.configuration;

import lombok.Data;

/**
 * Request to register a schema.
 */
@Data
public class Registration {

  private static final String VALUE = "-value";

  private String subject;
  private int version;
  private String reflectClass;

  /**
   * Appends {@code -value} to subject if necessary.
   */
  public void appendValueToSubject() {
    if (!subject.endsWith("-key") && !subject.endsWith(VALUE)) {
      subject += VALUE;
    }
  }
}
