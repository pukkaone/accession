package com.github.pukkaone.accession.schema.registry.maven;

import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility;
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType;
import org.apache.avro.SchemaCompatibility.SchemaPairCompatibility;

/**
 * How to compare new schema to existing registered schema for compatibility.
 */
public enum CompatibilityDirection {

  /**
   * New schema can decode data written with old schema.
   */
  BACKWARD {
    @Override
    public SchemaPairCompatibility check(Schema newSchema, Schema existingSchema) {
      return SchemaCompatibility.checkReaderWriterCompatibility(newSchema, existingSchema);
    }
  },

  /**
   * Old schema can decode data written with new schema.
   */
  FORWARD {
    @Override
    public SchemaPairCompatibility check(Schema newSchema, Schema existingSchema) {
      return SchemaCompatibility.checkReaderWriterCompatibility(existingSchema, newSchema);
    }
  },

  /**
   * Both old schema and new schema can decode data written by the other.
   */
  FULL {
    @Override
    public SchemaPairCompatibility check(Schema newSchema, Schema existingSchema) {
      var compatibility = BACKWARD.check(newSchema, existingSchema);
      return (compatibility.getType() == SchemaCompatibilityType.INCOMPATIBLE)
          ? compatibility
          : FORWARD.check(newSchema, existingSchema);
    }
  };

  /**
   * Checks schemas are compatible.
   *
   * @param newSchema
   *     proposed new schema
   * @param existingSchema
   *     existing registered schema
   * @return compatibility result
   */
  public abstract SchemaPairCompatibility check(Schema newSchema, Schema existingSchema);
}
