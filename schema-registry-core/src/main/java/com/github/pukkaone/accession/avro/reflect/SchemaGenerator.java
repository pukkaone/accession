package com.github.pukkaone.accession.avro.reflect;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilderException;
import org.apache.avro.reflect.ReflectData;

/**
 * Generates Avro schema by reflection.
 */
public final class SchemaGenerator {

  /**
   * Do not make schema from custom encoding nullable.
   */
  private static class StrictCustomEncodingAllowNull extends ReflectData {
    private static final StrictCustomEncodingAllowNull INSTANCE =
        new StrictCustomEncodingAllowNull();

    @Override
    protected Schema createFieldSchema(Field field, Map<String, Schema> names) {
      Schema schema = super.createFieldSchema(field, names);
      if (field.getType().isPrimitive()
          || schema.getProp(AbstractEncoding.CUSTOM_ENCODING) != null) {
        return schema;
      }

      return makeNullable(schema);
    }
  }

  private static ConcurrentHashMap<Integer, Schema> idToSchemaMap = new ConcurrentHashMap<>();

  // Disallow creating instances of this class.
  private SchemaGenerator() {
  }

  private static boolean isNullable(Class<?> type) {
    AvroReflect avroReflect = type.getAnnotation(AvroReflect.class);
    return avroReflect != null && avroReflect.nullable();
  }

  /**
   * Generates Avro schema by reflection.
   *
   * @param type
   *     Java type to introspect
   * @return schema
   */
  public static Schema getSchema(Class<?> type) {
    var reflectData = isNullable(type) ? StrictCustomEncodingAllowNull.INSTANCE : ReflectData.get();
    return reflectData.getSchema(type);
  }

  private static Schema computeReaderSchema(Schema writerSchema) {
    Class<?> readerType = ReflectData.get().getClass(writerSchema);
    if (readerType == null) {
      throw new SchemaBuilderException(
          "Class " + writerSchema.getFullName() + " not found");
    }

    return getSchema(readerType);
  }

  /**
   * Generates Avro schema for reading encoded data.
   *
   * @param schemaId
   *     schema ID
   * @param writerSchema
   *     writer schema
   * @return reader schema
   */
  public static Schema getReaderSchema(int schemaId, Schema writerSchema) {
    return idToSchemaMap.computeIfAbsent(
        schemaId,
        key -> computeReaderSchema(writerSchema));
  }
}
