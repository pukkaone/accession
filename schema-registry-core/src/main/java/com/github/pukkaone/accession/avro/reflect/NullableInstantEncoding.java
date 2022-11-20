package com.github.pukkaone.accession.avro.reflect;

import java.time.Instant;

/**
 * Encodes null or {@link Instant} value.
 */
public class NullableInstantEncoding extends NullableEncoding<Instant> {

  /**
   * Constructor.
   */
  public NullableInstantEncoding() {
    super(new InstantEncoding());
  }
}
