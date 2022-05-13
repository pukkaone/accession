package com.github.pukkaone.accession.avro.reflect;

import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;
import org.apache.avro.reflect.CustomEncoding;
import org.apache.avro.specific.SpecificData;

/**
 * Extends {@link CustomEncoding} to replace protected methods with public methods.
 *
 * @param <T>
 *     type of object to encode and decode
 */
public abstract class AbstractEncoding<T> extends CustomEncoding<T> {

  protected static final String CUSTOM_ENCODING = "CustomEncoding";
  protected static final String JAVA_CLASS = SpecificData.CLASS_PROP;
  protected static final String LOGICAL_TYPE = "logicalType";

  // Override method to make it public.
  @Override
  public Schema getSchema() {
    return super.getSchema();
  }

  /**
   * Encodes value.
   *
   * @param datum
   *     to encode
   * @param out
   *     output
   * @throws IOException
   *     if error occurred
   */
  public abstract void writeDatum(Object datum, Encoder out) throws IOException;

  /**
   * Decodes value.
   *
   * @param reuse
   *     object which may be reused to return value
   * @param in
   *     input
   * @return decoded value
   * @throws IOException
   *     if error occurred
   */
  public abstract T readDatum(Object reuse, Decoder in) throws IOException;

  @Override
  protected final void write(Object datum, Encoder out) throws IOException {
    writeDatum(datum, out);
  }

  @Override
  protected final T read(Object reuse, Decoder in) throws IOException {
    return readDatum(reuse, in);
  }
}
