package com.github.pukkaone.accession.avro.reflect;

import java.io.IOException;
import java.time.Instant;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

/**
 * Encodes {@link Instant} value.
 */
public class InstantEncoding extends AbstractEncoding<Instant> {

  /**
   * Constructor.
   */
  public InstantEncoding() {
    schema = Schema.create(Schema.Type.STRING);
    schema.addProp(CUSTOM_ENCODING, InstantEncoding.class.getName());
    schema.addProp(JAVA_CLASS, Instant.class.getName());
  }

  @Override
  public void writeDatum(Object datum, Encoder out) throws IOException {
    Instant instant = (Instant) datum;
    out.writeString(instant.toString());
  }

  @Override
  public Instant readDatum(Object reuse, Decoder in) throws IOException {
    return Instant.parse(in.readString());
  }
}
