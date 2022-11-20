package com.github.pukkaone.accession.avro.reflect;

import java.io.IOException;
import java.time.LocalTime;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

/**
 * Encodes {@link LocalTime} value.
 */
public class LocalTimeEncoding extends AbstractEncoding<LocalTime> {

  /**
   * Constructor.
   */
  public LocalTimeEncoding() {
    schema = Schema.create(Schema.Type.STRING);
    schema.addProp(CUSTOM_ENCODING, LocalTimeEncoding.class.getName());
    schema.addProp(JAVA_CLASS, LocalTime.class.getName());
    schema.addProp(LOGICAL_TYPE, LocalTime.class.getSimpleName());
  }

  @Override
  public void writeDatum(Object datum, Encoder out) throws IOException {
    LocalTime time = (LocalTime) datum;
    out.writeString(time.toString());
  }

  @Override
  public LocalTime readDatum(Object reuse, Decoder in) throws IOException {
    return LocalTime.parse(in.readString());
  }
}
