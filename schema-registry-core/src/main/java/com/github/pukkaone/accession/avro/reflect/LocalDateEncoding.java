package com.github.pukkaone.accession.avro.reflect;

import java.io.IOException;
import java.time.LocalDate;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

/**
 * Encodes {@link LocalDate} value.
 */
public class LocalDateEncoding extends AbstractEncoding<LocalDate> {

  /**
   * Constructor.
   */
  public LocalDateEncoding() {
    schema = Schema.create(Schema.Type.STRING);
    schema.addProp(CUSTOM_ENCODING, LocalDateEncoding.class.getName());
    schema.addProp(JAVA_CLASS, LocalDate.class.getName());
    schema.addProp(LOGICAL_TYPE, LocalDate.class.getSimpleName());
  }

  @Override
  public void writeDatum(Object datum, Encoder out) throws IOException {
    LocalDate dateTime = (LocalDate) datum;
    out.writeString(dateTime.toString());
  }

  @Override
  public LocalDate readDatum(Object reuse, Decoder in) throws IOException {
    return LocalDate.parse(in.readString());
  }
}
