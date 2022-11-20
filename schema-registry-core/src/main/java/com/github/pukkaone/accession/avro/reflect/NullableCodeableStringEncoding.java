package com.github.pukkaone.accession.avro.reflect;

import com.github.pukkaone.accession.persistence.Codeable;
import java.lang.reflect.ParameterizedType;

/**
 * Encodes enum value as a code in an Avro string, or null if enum value is null.
 *
 * @param <E>
 *     enum type, must implement {@link Codeable}{@code <String>} interface
 */
public abstract class NullableCodeableStringEncoding<E extends Enum<E> & Codeable<String>>
    extends NullableEncoding<E> {

  /**
   * Constructor.
   */
  protected NullableCodeableStringEncoding() {
    initialize(createCodeableEncoding());
  }

  @SuppressWarnings("unchecked")
  private CodeableStringEncoding<E> createCodeableEncoding() {
    Class<E> enumClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
    return new CodeableStringEncoding<>(enumClass);
  }
}
