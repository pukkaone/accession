package com.github.pukkaone.accession.avro.reflect;

import java.time.LocalDateTime;

/**
 * Encodes null or {@link LocalDateTime} value.
 */
public class NullableLocalDateTimeEncoding extends NullableEncoding<LocalDateTime> {

  /**
   * Constructor.
   */
  public NullableLocalDateTimeEncoding() {
    super(new LocalDateTimeEncoding());
  }
}
