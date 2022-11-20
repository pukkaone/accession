package com.github.pukkaone.accession.avro.reflect;

import java.io.IOException;
import org.apache.avro.UnresolvedUnionException;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;
import org.apache.avro.reflect.ReflectData;

/**
 * Encodes null or a present value.
 *
 * @param <T>
 *     type of object to encode and decode
 */
public abstract class NullableEncoding<T> extends AbstractEncoding<T> {

  private AbstractEncoding<T> payloadEncoding;

  /**
   * Constructor.
   */
  protected NullableEncoding() {
  }

  /**
   * Constructor.
   *
   * @param payloadEncoding
   *     encoding for present value
   */
  protected NullableEncoding(AbstractEncoding<T> payloadEncoding) {
    initialize(payloadEncoding);
  }

  /**
   * Initializes encoding.
   *
   * @param payloadEncoding
   *     encoding for present value
   */
  protected final void initialize(AbstractEncoding<T> payloadEncoding) {
    this.payloadEncoding = payloadEncoding;

    schema = ReflectData.makeNullable(payloadEncoding.getSchema());
  }

  @Override
  public void writeDatum(Object datum, Encoder out) throws IOException {
    if (datum == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      payloadEncoding.writeDatum(datum, out);
    }
  }

  @Override
  public T readDatum(Object reuse, Decoder in) throws IOException {
    int index = in.readIndex();
    switch (index) {
      case 0:
        in.readNull();
        return null;
      case 1:
        return payloadEncoding.readDatum(null, in);
      default:
        throw new UnresolvedUnionException(schema, index);
    }
  }
}
