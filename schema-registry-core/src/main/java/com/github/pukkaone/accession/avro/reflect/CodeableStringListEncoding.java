package com.github.pukkaone.accession.avro.reflect;

import com.github.pukkaone.accession.persistence.Codeable;
import java.lang.reflect.ParameterizedType;

/**
 * Encodes list of enum by encoding each enum value as a code in an Avro string.
 *
 * @param <E>
 *     enum type, must implement {@link Codeable}{@code <String>} interface
 */
public abstract class CodeableStringListEncoding<E extends Enum<E> & Codeable<String>>
    extends ListEncoding<E> {

  /**
   * Constructor.
   */
  protected CodeableStringListEncoding() {
    initialize(createCodeableEncoding());
  }

  @SuppressWarnings("unchecked")
  private CodeableStringEncoding<E> createCodeableEncoding() {
    Class<E> enumClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
    return new CodeableStringEncoding<>(enumClass);
  }
}
