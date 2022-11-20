package com.github.pukkaone.accession.avro.reflect;

import com.github.pukkaone.accession.persistence.Codeable;
import java.lang.reflect.ParameterizedType;
import java.util.Set;

/**
 * Encodes set of enum by encoding each enum value as a code in an Avro string, or null if set is
 * null.
 *
 * @param <E>
 *     enum type, must implement {@link Codeable}{@code <String>} interface
 */
public abstract class NullableCodeableStringSetEncoding<E extends Enum<E> & Codeable<String>>
    extends NullableEncoding<Set<E>> {

  /**
   * Constructor.
   */
  protected NullableCodeableStringSetEncoding() {
    SetEncoding<E> collectionEncoding = new SetEncoding<>(createCodeableEncoding());
    initialize(collectionEncoding);
  }

  @SuppressWarnings("unchecked")
  private CodeableStringEncoding<E> createCodeableEncoding() {
    Class<E> enumClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
    return new CodeableStringEncoding<>(enumClass);
  }
}
