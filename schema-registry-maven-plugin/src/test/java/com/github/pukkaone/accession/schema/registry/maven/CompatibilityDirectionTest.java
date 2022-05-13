package com.github.pukkaone.accession.schema.registry.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link CompatibilityDirection} test.
 */
class CompatibilityDirectionTest {

  private static final Schema MANDATORY_FIELD_SCHEMA = SchemaBuilder.record("Record").fields()
      .name("field").type().intType().noDefault()
      .endRecord();

  private static final Schema OPTIONAL_FIELD_SCHEMA = SchemaBuilder.record("Record").fields()
      .optionalInt("field")
      .endRecord();

  static Stream<Arguments> given_compatibility_mode_when_check_then_compatibility_type() {
    return Stream.of(
/* TODO: Add these tests after https://issues.apache.org/jira/browse/AVRO-3612 is resolved.

        Arguments.of(
            CompatibilityDirection.BACKWARD,
            MANDATORY_FIELD_SCHEMA,
            OPTIONAL_FIELD_SCHEMA,
            SchemaCompatibilityType.INCOMPATIBLE,
            "/fields/0/type/0"),
        Arguments.of(
            CompatibilityDirection.FORWARD,
            OPTIONAL_FIELD_SCHEMA,
            MANDATORY_FIELD_SCHEMA,
            SchemaCompatibilityType.INCOMPATIBLE,
            "/fields/0/type/0"),
        Arguments.of(
            CompatibilityDirection.FULL,
            MANDATORY_FIELD_SCHEMA,
            OPTIONAL_FIELD_SCHEMA,
            SchemaCompatibilityType.INCOMPATIBLE,
            "/fields/0/type/0"),
         Arguments.of(
            CompatibilityDirection.FULL,
            OPTIONAL_FIELD_SCHEMA,
            MANDATORY_FIELD_SCHEMA,
            SchemaCompatibilityType.INCOMPATIBLE,
            "/fields/0/type/0"),
*/
        Arguments.of(
            CompatibilityDirection.BACKWARD,
            OPTIONAL_FIELD_SCHEMA,
            MANDATORY_FIELD_SCHEMA,
            SchemaCompatibilityType.COMPATIBLE,
            null),
        Arguments.of(
            CompatibilityDirection.FORWARD,
            MANDATORY_FIELD_SCHEMA,
            OPTIONAL_FIELD_SCHEMA,
            SchemaCompatibilityType.COMPATIBLE,
            null));
  }

  @MethodSource
  @ParameterizedTest
  void given_compatibility_mode_when_check_then_compatibility_type(
      CompatibilityDirection compatibilityDirection,
      Schema newSchema,
      Schema existingSchema,
      SchemaCompatibilityType expectedCompatiblityType,
      String expectedLocation) {

    var compatibility = compatibilityDirection.check(newSchema, existingSchema);
    assertThat(compatibility.getType()).isEqualTo(expectedCompatiblityType);
    if (compatibility.getType() == SchemaCompatibilityType.INCOMPATIBLE) {
      var location = compatibility.getResult().getIncompatibilities().get(0).getLocation();
      assertThat(location).isEqualTo(expectedLocation);
    }
  }
}
