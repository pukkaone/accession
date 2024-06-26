package com.github.pukkaone.accession.avro.reflect;

import com.github.pukkaone.accession.persistence.CodeToEnumMapper;
import com.github.pukkaone.accession.persistence.Codeable;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

/**
 * Encodes enum constant as a code in an Avro string.
 *
 * @param <E>
 *     enum type, must implement {@link Codeable}{@code <String>} interface
 */
public class CodeableStringEncoding<E extends Enum<E> & Codeable<String>>
    extends AbstractEncoding<E> {

  private String enumClassName;
  private CodeToEnumMapper<String, E> mapper;

  /**
   * Constructor.
   *
   * @param enumClass
   *     enum type
   */
  protected CodeableStringEncoding(Class<E> enumClass) {
    initialize(enumClass);
  }

  /**
   * Constructor.
   */
  @SuppressWarnings("unchecked")
  protected CodeableStringEncoding() {
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

    schema = Schema.create(Schema.Type.STRING);
    schema.addProp(CUSTOM_ENCODING, CodeableStringEncoding.class.getName());
    schema.addProp(JAVA_CLASS, enumClassName);

    mapper = new CodeToEnumMapper<>(enumClass);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void writeDatum(Object datum, Encoder out) throws IOException {
    out.writeString(((E) datum).getCode());
  }

  @Override
  public E readDatum(Object reuse, Decoder in) throws IOException {
    String code = in.readString();
    return mapper.fromCode(code)
        .orElseThrow(() -> new IllegalArgumentException(
            "Cannot map unknown code [" + code + "] to " + enumClassName));
  }
}
