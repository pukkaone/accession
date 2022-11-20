package com.github.pukkaone.accession.schema.registry.rule;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.pukkaone.accession.avro.reflect.SchemaGenerator;
import java.time.Instant;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.reflect.Nullable;
import org.junit.jupiter.api.Test;

/**
 * {@link DefaultEncodingRule} test.
 */
class DefaultEncodingRuleTest {

  private static class BadRecord {
    private Instant updatedAt;
  }

  private static class NullableBadRecord {
    @Nullable
    private Instant updatedAt;
  }

  private static class ParentBadRecord {
    @Nullable
    private NullableBadRecord nullableBadRecord;
  }

  private static class ListBadRecord {
    private List<BadRecord> badRecords;
  }

  private SchemaValidator schemaValidator = new SchemaValidator();

  @Test
  void when_default_encoding_then_find_violation() {
    Schema schema = SchemaGenerator.getSchema(BadRecord.class);
    List<RuleViolation> violations = schemaValidator.validate(schema);

    assertThat(violations.size()).isEqualTo(1);
    RuleViolation violation = violations.get(0);
    assertThat(violation.toString()).isEqualTo(
        "Field updatedAt: " +
            "java.time.Instant must be encoded by @AvroEncode using a custom encoding.");
  }

  @Test
  void when_nullable_default_encoding_then_find_violation() {
    Schema schema = SchemaGenerator.getSchema(NullableBadRecord.class);
    List<RuleViolation> violations = schemaValidator.validate(schema);

    assertThat(violations.size()).isEqualTo(1);
    RuleViolation violation = violations.get(0);
    assertThat(violation.toString()).isEqualTo(
        "Field updatedAt: " +
            "java.time.Instant must be encoded by @AvroEncode using a custom encoding.");
  }

  @Test
  void when_nested_nullable_default_encoding_then_find_violation() {
    Schema schema = SchemaGenerator.getSchema(ParentBadRecord.class);
    List<RuleViolation> violations = schemaValidator.validate(schema);

    assertThat(violations.size()).isEqualTo(1);
    RuleViolation violation = violations.get(0);
    assertThat(violation.toString()).isEqualTo(
        "Field nullableBadRecord.updatedAt: " +
            "java.time.Instant must be encoded by @AvroEncode using a custom encoding.");
  }

  @Test
  void when_list_default_encoding_then_find_violation() {
    Schema schema = SchemaGenerator.getSchema(ListBadRecord.class);
    List<RuleViolation> violations = schemaValidator.validate(schema);

    assertThat(violations.size()).isEqualTo(1);
    RuleViolation violation = violations.get(0);
    assertThat(violation.toString()).isEqualTo(
        "Field badRecords.updatedAt: " +
            "java.time.Instant must be encoded by @AvroEncode using a custom encoding.");
  }
}
