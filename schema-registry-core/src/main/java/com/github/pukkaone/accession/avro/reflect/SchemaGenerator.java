package com.github.pukkaone.accession.avro.reflect;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilderException;
import org.apache.avro.reflect.ReflectData;

/**
 * Generates Avro schema by reflection.
 */
public final class SchemaGenerator {

  private static ConcurrentHashMap<Integer, Schema> idToSchemaMap = new ConcurrentHashMap<>();
  private static ConcurrentHashMap<Class<?>, Schema> typeToSchemaMap = new ConcurrentHashMap<>();

  // Disallow creating instances of this class.
  private SchemaGenerator() {
  }

  private static boolean isNullable(Class<?> type) {
    AvroReflect avroReflect = type.getAnnotation(AvroReflect.class);
    return avroReflect != null && avroReflect.nullable();
  }

  private static Schema doGetSchema(Class<?> type) {
    ReflectData reflectData = isNullable(type) ? ReflectData.AllowNull.get() : ReflectData.get();
    return reflectData.getSchema(type);
  }

  private static Schema computeReaderSchema(Schema writerSchema) {
    Class<?> readerType = ReflectData.get().getClass(writerSchema);
    if (readerType == null) {
      throw new SchemaBuilderException(
          "Class " + writerSchema.getFullName() + " not found");
    }

    return doGetSchema(readerType);
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

  /**
   * Generates Avro schema by reflection.
   *
   * @param type
   *     Java type to introspect
   * @return schema
   */
  public static Schema getSchema(Class<?> type) {
    return typeToSchemaMap.computeIfAbsent(type, SchemaGenerator::doGetSchema);
  }
}
