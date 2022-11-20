package com.github.pukkaone.accession.avro.reflect;

import java.time.LocalDate;

/**
 * Encodes null or {@link LocalDate} value.
 */
public class NullableLocalDateEncoding extends NullableEncoding<LocalDate> {

  /**
   * Constructor.
   */
  public NullableLocalDateEncoding() {
    super(new LocalDateEncoding());
  }
}
