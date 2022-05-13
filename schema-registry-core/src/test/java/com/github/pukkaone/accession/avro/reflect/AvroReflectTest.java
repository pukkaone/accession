package com.github.pukkaone.accession.avro.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link AvroReflect#nullable() @AvroReflect(nullable=...} attribute.
 */
class AvroReflectTest {

  public static class Message {
    public String name;
  }

  @AvroReflect(nullable = true)
  public static class NullableMessage {
    public String name;
  }

  @Test
  public void given_nullable_false_when_generate_schema_then_field_is_not_nullable() {
    Schema schema = SchemaGenerator.getSchema(Message.class);
    assertThat(schema.getField("name").schema().getType()).isEqualTo(Schema.Type.STRING);
  }

  @Test
  public void given_nullable_true_when_generate_schema_then_field_is_nullable() {
    Schema schema = SchemaGenerator.getSchema(NullableMessage.class);
    assertThat(schema.getField("name").schema().getType()).isEqualTo(Schema.Type.UNION);
  }
}
