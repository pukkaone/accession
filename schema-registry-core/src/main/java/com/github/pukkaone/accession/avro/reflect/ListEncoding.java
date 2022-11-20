package com.github.pukkaone.accession.avro.reflect;

import java.util.ArrayList;
import java.util.List;

/**
 * Encodes a list by encoding each element with a custom encoding.
 *
 * @param <E>
 *     element type
 */
public class ListEncoding<E> extends CollectionEncoding<List<E>, E> {

  /**
   * Constructor.
   */
  protected ListEncoding() {
  }

  /**
   * Constructor.
   *
   * @param elementEncoding
   *     encoding for element
   */
  protected ListEncoding(AbstractEncoding<E> elementEncoding) {
    initialize(elementEncoding);
  }

  /**
   * Initializes encoding.
   *
   * @param elementEncoding
   *     encoding for element
   */
  protected final void initialize(AbstractEncoding<E> elementEncoding) {
    super.initialize(elementEncoding, ArrayList::new);
  }
}
