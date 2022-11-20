package com.github.pukkaone.accession.schema.registry.rule;

import org.apache.avro.Schema;

/**
 * Rule convenience methods.
 */
public final class Rules {

  // Private constructor disallows creating instances of this class.
  private Rules() {
  }

  /**
   * If the input schema is a union with first type null, then return the union's second type,
   * otherwise return the input schema.
   *
   * @param schema
   *     to transform
   * @return schema
   */
  public static Schema getSchemaFromNullable(Schema schema) {
    if (schema.getType() == Schema.Type.UNION
        && schema.getTypes().get(0).getType() == Schema.Type.NULL) {
      return schema.getTypes().get(1);
    }

    return schema;
  }
}
