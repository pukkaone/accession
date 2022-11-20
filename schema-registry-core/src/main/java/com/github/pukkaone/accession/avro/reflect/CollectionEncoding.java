package com.github.pukkaone.accession.avro.reflect;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Supplier;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

/**
 * Encodes a collection by encoding each element with a custom encoding.
 *
 * @param <C>
 *     collection type
 * @param <E>
 *     element type
 */
public abstract class CollectionEncoding<C extends Collection<E>, E> extends AbstractEncoding<C> {

  private AbstractEncoding<E> elementEncoding;
  private Supplier<C> collectionSupplier;

  /**
   * Constructor.
   */
  protected CollectionEncoding() {
  }

  /**
   * Constructor.
   *
   * @param elementEncoding
   *     encoding for element
   * @param collectionSupplier
   *     creates collection for decoded data
   */
  protected CollectionEncoding(
      AbstractEncoding<E> elementEncoding, Supplier<C> collectionSupplier) {

    initialize(elementEncoding, collectionSupplier);
  }

  /**
   * Initializes encoding.
   *
   * @param elementEncoding
   *     encoding for element
   * @param collectionSupplier
   *     creates collection for decoded data
   */
  protected final void initialize(
      AbstractEncoding<E> elementEncoding, Supplier<C> collectionSupplier) {

    this.elementEncoding = elementEncoding;
    this.collectionSupplier = collectionSupplier;

    Schema elementSchema = elementEncoding.getSchema();
    schema = Schema.createArray(elementSchema);
    schema.addProp(CUSTOM_ENCODING, getClass().getName());
  }

  @Override
  @SuppressWarnings("unchecked")
  public void writeDatum(Object datum, Encoder out) throws IOException {
    Collection<E> collection = (Collection<E>) datum;
    out.writeArrayStart();
    int size = (collection == null) ? 0 : collection.size();
    out.setItemCount(size);
    if (size > 0) {
      for (E element : collection) {
        out.startItem();
        elementEncoding.writeDatum(element, out);
      }
    }

    out.writeArrayEnd();
  }

  @Override
  public C readDatum(Object reuse, Decoder in) throws IOException {
    C collection = collectionSupplier.get();
    for (long i = in.readArrayStart(); i != 0; i = in.arrayNext()) {
      for (long j = 0; j < i; ++j) {
        E element = elementEncoding.readDatum(null, in);
        collection.add(element);
      }
    }

    return collection;
  }
}
