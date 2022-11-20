package com.github.pukkaone.accession.avro.reflect;

import java.io.IOException;
import java.time.LocalDateTime;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

/**
 * Encodes {@link LocalDateTime} value.
 */
public class LocalDateTimeEncoding extends AbstractEncoding<LocalDateTime> {

  /**
   * Constructor.
   */
  public LocalDateTimeEncoding() {
    schema = Schema.create(Schema.Type.STRING);
    schema.addProp(CUSTOM_ENCODING, LocalDateTimeEncoding.class.getName());
    schema.addProp(JAVA_CLASS, LocalDateTime.class.getName());
  }

  @Override
  public void writeDatum(Object datum, Encoder out) throws IOException {
    LocalDateTime dateTime = (LocalDateTime) datum;
    out.writeString(dateTime.toString());
  }

  @Override
  public LocalDateTime readDatum(Object reuse, Decoder in) throws IOException {
    return LocalDateTime.parse(in.readString());
  }
}
