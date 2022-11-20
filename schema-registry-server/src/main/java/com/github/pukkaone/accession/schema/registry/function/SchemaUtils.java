package com.github.pukkaone.accession.schema.registry.function;

import java.util.Arrays;
import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;
import org.apache.avro.SchemaValidationException;
import org.apache.avro.SchemaValidator;
import org.apache.avro.SchemaValidatorBuilder;

/**
 * Avro schema functions.
 */
public final class SchemaUtils {

  private static final SchemaValidator BACKWARD_VALIDATOR =
      new SchemaValidatorBuilder().canReadStrategy().validateLatest();

  // Private constructor disallows creating instances of this class.
  private SchemaUtils() {
  }

  private static Schema parse(String input) {
    return new Schema.Parser().parse(input);
  }

  private static boolean isCompatible(Schema newSchema, Schema existingSchema) {
    try {
      BACKWARD_VALIDATOR.validate(newSchema, Arrays.asList(existingSchema));
    } catch (SchemaValidationException e) {
      return false;
    }
    return true;
  }

  /**
   * Checks schemas are compatible.
   *
   * @param newSchema
   *     proposed new schema
   * @param existingSchema
   *     existing registered schema
   * @return true if new schema is compatible with existing schema
   */
  public static boolean isCompatible(String newSchema, String existingSchema) {
    return isCompatible(parse(newSchema), parse(existingSchema));
  }

  /**
   * Computes hash of normalized schema.
   *
   * @param schema
   *     input schema
   * @return hash
   */
  public static long fingerprint(String schema) {
    return SchemaNormalization.parsingFingerprint64(parse(schema));
  }
}
