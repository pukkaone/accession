package com.github.pukkaone.accession.avro.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.apache.avro.Schema;
import org.apache.avro.reflect.AvroEncode;
import org.junit.jupiter.api.Test;

/**
 * {@link SchemaGenerator} tests.
 */
class SchemaGeneratorTest {

  public static class Message {
    public String name;
  }

  @AvroReflect(nullable = true)
  public static class AvroReflectNullableMessage {
    public String name;

    @AvroEncode(using = LocalDateEncoding.class)
    private LocalDate localDate;
  }

  @Test
  void given_not_nullable_field_then_schema_is_not_nullable() {
    Schema schema = SchemaGenerator.getSchema(Message.class);
    assertThat(schema.getField("name").schema().getType()).isEqualTo(Schema.Type.STRING);
  }

  @Test
  void given_avroreflect_nullable_class_then_schema_is_nullable() {
    Schema schema = SchemaGenerator.getSchema(AvroReflectNullableMessage.class);
    assertThat(schema.getField("name").schema().getType()).isEqualTo(Schema.Type.UNION);
  }

  @Test
  void given_avroreflect_nullable_class_and_custom_encoding_then_schema_is_not_nullable() {
    Schema schema = SchemaGenerator.getSchema(AvroReflectNullableMessage.class);
    Schema fieldSchema = schema.getField("localDate").schema();
    assertThat(fieldSchema.getType()).isEqualTo(Schema.Type.STRING);
  }
}
