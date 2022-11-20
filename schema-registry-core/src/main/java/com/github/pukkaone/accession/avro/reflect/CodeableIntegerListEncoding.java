package com.github.pukkaone.accession.avro.reflect;

import com.github.pukkaone.accession.persistence.Codeable;
import java.lang.reflect.ParameterizedType;

/**
 * Encodes list of enum by encoding each enum value as a code in an Avro integer.
 *
 * @param <E>
 *     enum type, must implement {@link Codeable}{@code <Integer>} interface
 */
public abstract class CodeableIntegerListEncoding<E extends Enum<E> & Codeable<Integer>>
    extends ListEncoding<E> {

  /**
   * Constructor.
   */
  protected CodeableIntegerListEncoding() {
    initialize(createCodeableEncoding());
  }

  @SuppressWarnings("unchecked")
  private CodeableIntegerEncoding<E> createCodeableEncoding() {
    Class<E> enumClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
    return new CodeableIntegerEncoding<>(enumClass);
  }
}
