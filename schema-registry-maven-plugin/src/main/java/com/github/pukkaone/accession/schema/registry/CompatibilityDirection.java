package com.github.pukkaone.accession.schema.registry;

import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility;
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType;
import org.apache.avro.SchemaCompatibility.SchemaPairCompatibility;

/**
 * How to compare new schema to already registered schema for compatibility.
 */
public enum CompatibilityDirection {

  /**
   * New schema can decode data written with old schema.
   */
  BACKWARD {
    @Override
    public SchemaPairCompatibility check(Schema newSchema, Schema registeredSchema) {
      return SchemaCompatibility.checkReaderWriterCompatibility(newSchema, registeredSchema);
    }
  },

  /**
   * Old schema can decode data written with new schema.
   */
  FORWARD {
    @Override
    public SchemaPairCompatibility check(Schema newSchema, Schema registeredSchema) {
      return SchemaCompatibility.checkReaderWriterCompatibility(registeredSchema, newSchema);
    }
  },

  /**
   * Both old schema and new schema can decode data written by the other.
   */
  FULL {
    @Override
    public SchemaPairCompatibility check(Schema newSchema, Schema registeredSchema) {
      var compatibility = BACKWARD.check(newSchema, registeredSchema);
      return (compatibility.getType() == SchemaCompatibilityType.INCOMPATIBLE)
          ? compatibility
          : FORWARD.check(newSchema, registeredSchema);
    }
  };

  /**
   * Checks schemas are compatible.
   *
   * @param newSchema
   *     proposed new schema
   * @param registeredSchema
   *     already registered schema
   * @return compatibility result
   */
  public abstract SchemaPairCompatibility check(Schema newSchema, Schema registeredSchema);
}
