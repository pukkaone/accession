package com.github.pukkaone.accession.avro.reflect;

import com.github.pukkaone.accession.persistence.Codeable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Encodes list of enum by encoding each enum value as a code in an Avro string, or null if list is
 * null.
 *
 * @param <E>
 *     enum type, must implement {@link Codeable}{@code <String>} interface
 */
public abstract class NullableCodeableStringListEncoding<E extends Enum<E> & Codeable<String>>
    extends NullableEncoding<List<E>> {

  /**
   * Constructor.
   */
  protected NullableCodeableStringListEncoding() {
    ListEncoding<E> collectionEncoding = new ListEncoding<>(createCodeableEncoding());
    initialize(collectionEncoding);
  }

  @SuppressWarnings("unchecked")
  private CodeableStringEncoding<E> createCodeableEncoding() {
    Class<E> enumClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
    return new CodeableStringEncoding<>(enumClass);
  }
}
