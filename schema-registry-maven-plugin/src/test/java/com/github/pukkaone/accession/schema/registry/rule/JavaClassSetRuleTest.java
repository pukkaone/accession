package com.github.pukkaone.accession.schema.registry.rule;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.pukkaone.accession.avro.reflect.SchemaGenerator;
import java.util.List;
import java.util.Set;
import org.apache.avro.Schema;
import org.apache.avro.reflect.Nullable;
import org.junit.jupiter.api.Test;

/**
 * {@link JavaClassSetRule} test.
 */
class JavaClassSetRuleTest {

  private static class BadRecord {
    private Set<String> tags;
  }

  private static class NullableBadRecord {
    @Nullable
    private Set<String> tags;
  }

  private SchemaValidator schemaValidator = new SchemaValidator();

  @Test
  void when_java_class_is_set_then_find_violation() {
    Schema schema = SchemaGenerator.getSchema(BadRecord.class);
    List<RuleViolation> violations = schemaValidator.validate(schema);

    assertThat(violations.size()).isEqualTo(1);
    RuleViolation violation = violations.get(0);
    assertThat(violation.getRule().getClass()).isEqualTo(JavaClassSetRule.class);
    assertThat(violation.getFieldPath()).isEqualTo("tags");
  }

  @Test
  void when_nullable_java_class_is_set_then_find_violation() {
    Schema schema = SchemaGenerator.getSchema(NullableBadRecord.class);
    List<RuleViolation> violations = schemaValidator.validate(schema);

    assertThat(violations.size()).isEqualTo(1);
    RuleViolation violation = violations.get(0);
    assertThat(violation.getRule().getClass()).isEqualTo(JavaClassSetRule.class);
    assertThat(violation.getFieldPath()).isEqualTo("tags");
  }
}
