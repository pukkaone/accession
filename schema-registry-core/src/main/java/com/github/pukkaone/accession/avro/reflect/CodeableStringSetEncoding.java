package com.github.pukkaone.accession.avro.reflect;

import com.github.pukkaone.accession.persistence.Codeable;
import java.lang.reflect.ParameterizedType;

/**
 * Encodes set of enum by encoding each enum value as a code in an Avro string.
 *
 * @param <E>
 *     enum type, must implement {@link Codeable}{@code <String>} interface
 */
public abstract class CodeableStringSetEncoding<E extends Enum<E> & Codeable<String>>
    extends SetEncoding<E> {

  /**
   * Constructor.
   */
  protected CodeableStringSetEncoding() {
    initialize(createCodeableEncoding());
  }

  @SuppressWarnings("unchecked")
  private CodeableStringEncoding<E> createCodeableEncoding() {
    Class<E> enumClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
    return new CodeableStringEncoding<>(enumClass);
  }
}
