package com.github.pukkaone.accession.schema.registry.maven.rule;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.pukkaone.accession.avro.reflect.SchemaGenerator;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.reflect.Nullable;
import org.junit.jupiter.api.Test;

/**
 * {@link LocalDateDefaultEncodingRule} test.
 */
class LocalDateTimeDefaultEncodingRuleTest {

  private static class BadRecord {
    private LocalDateTime updatedAt;
  }

  private static class NullableBadRecord {
    @Nullable
    private LocalDateTime updatedAt;
  }

  private SchemaValidator schemaValidator = new SchemaValidator();

  @Test
  void when_default_encoding_then_find_violation() {
    Schema schema = SchemaGenerator.getSchema(BadRecord.class);
    List<RuleViolation> violations = schemaValidator.validate(schema);

    assertThat(violations.size()).isEqualTo(1);
    RuleViolation violation = violations.get(0);
    assertThat(violation.getRule().getClass()).isEqualTo(LocalDateTimeDefaultEncodingRule.class);
    assertThat(violation.getFieldPath()).isEqualTo("updatedAt");
  }

  @Test
  void when_nullable_default_encoding_then_find_violation() {
    Schema schema = SchemaGenerator.getSchema(NullableBadRecord.class);
    List<RuleViolation> violations = schemaValidator.validate(schema);

    assertThat(violations.size()).isEqualTo(1);
    RuleViolation violation = violations.get(0);
    assertThat(violation.getRule().getClass()).isEqualTo(LocalDateTimeDefaultEncodingRule.class);
    assertThat(violation.getFieldPath()).isEqualTo("updatedAt");
  }
}
