package com.github.pukkaone.accession.avro.reflect;

import java.time.LocalTime;

/**
 * Encodes null or {@link LocalTime} value.
 */
public class NullableLocalTimeEncoding extends NullableEncoding<LocalTime> {

  /**
   * Constructor.
   */
  public NullableLocalTimeEncoding() {
    super(new LocalTimeEncoding());
  }
}
