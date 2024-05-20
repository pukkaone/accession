package com.github.pukkaone.accession.avro.reflect;

import com.github.pukkaone.accession.persistence.CodeToEnumMapper;
import com.github.pukkaone.accession.persistence.Codeable;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

/**
 * Encodes enum constant as a code in an Avro integer.
 *
 * @param <E>
 *     enum type, must implement {@link Codeable}{@code <Integer>} interface
 */
public class CodeableIntegerEncoding<E extends Enum<E> & Codeable<Integer>>
    extends AbstractEncoding<E> {

  private String enumClassName;
  private CodeToEnumMapper<Integer, E> mapper;

  /**
   * Constructor.
   *
   * @param enumClass
   *     enum type
   */
  protected CodeableIntegerEncoding(Class<E> enumClass) {
    initialize(enumClass);
  }

  /**
   * Constructor.
   */
  @SuppressWarnings("unchecked")
  protected CodeableIntegerEncoding() {
    Class<E> enumClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
    initialize(enumClass);
  }

  /**
   * Prepares encoding for enum.
   *
   * @param enumClass
   *     enum class
   */
  protected void initialize(Class<E> enumClass) {
    enumClassName = enumClass.getName();

    schema = Schema.create(Schema.Type.INT);
    schema.addProp(CUSTOM_ENCODING, CodeableIntegerEncoding.class.getName());
    schema.addProp(JAVA_CLASS, enumClassName);

    mapper = new CodeToEnumMapper<>(enumClass);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void writeDatum(Object datum, Encoder out) throws IOException {
    out.writeInt(((E) datum).getCode());
  }

  @Override
  public E readDatum(Object reuse, Decoder in) throws IOException {
    int code = in.readInt();
    return mapper.fromCode(code)
        .orElseThrow(() -> new IllegalArgumentException(
            "Cannot map unknown code [" + code + "] to " + enumClassName));
  }
}
