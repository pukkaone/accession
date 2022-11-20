package com.github.pukkaone.accession.avro.reflect;

import java.util.HashSet;
import java.util.Set;

/**
 * Encodes a set by encoding each element with a custom encoding.
 *
 * @param <E>
 *     element type
 */
public class SetEncoding<E> extends CollectionEncoding<Set<E>, E> {

  /**
   * Constructor.
   */
  protected SetEncoding() {
  }

  /**
   * Constructor.
   *
   * @param elementEncoding
   *     encoding for element
   */
  protected SetEncoding(AbstractEncoding<E> elementEncoding) {
    initialize(elementEncoding);
  }

  /**
   * Initializes encoding.
   *
   * @param elementEncoding
   *     encoding for element
   */
  protected final void initialize(AbstractEncoding<E> elementEncoding) {
    super.initialize(elementEncoding, HashSet::new);
  }
}
