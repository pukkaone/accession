package com.github.pukkaone.accession.schema.registry.function;

import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;

/**
 * Avro schema functions.
 */
public final class SchemaUtils {

  // Private constructor disallows creating instances of this class.
  private SchemaUtils() {
  }

  private static Schema parse(String input) {
    return new Schema.Parser().parse(input);
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
