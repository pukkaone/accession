package com.github.pukkaone.accession.schema.registry.rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import org.apache.avro.Schema;

/**
 * Finds rule violations in schema.
 */
public class SchemaValidator {

  private List<Rule> rules = new ArrayList<>();
  private Set<Schema> seen = new HashSet<>();

  /**
   * Constructor.
   */
  public SchemaValidator() {
    ServiceLoader<Rule> serviceLoader = ServiceLoader.load(Rule.class);
    for (Rule rule : serviceLoader) {
      rules.add(rule);
    }
  }

  private void findViolations(RuleContext ruleContext, Schema.Field field) {
    ruleContext.push(field.name());
    try {
      for (Rule rule : rules) {
        rule.findViolations(ruleContext, field);
      }

      findViolations(ruleContext, field.schema());
    } finally {
      ruleContext.pop();
    }
  }

  private void findViolations(RuleContext ruleContext, Schema inputSchema) {
    Schema schema = Rules.getSchemaFromNullable(inputSchema);
    if (seen.contains(schema)) {
      return;
    }
    seen.add(schema);

    if (schema.getType() == Schema.Type.ARRAY) {
      findViolations(ruleContext, schema.getElementType());
    } else if (schema.getType() == Schema.Type.RECORD) {
      for (Schema.Field field : schema.getFields()) {
        findViolations(ruleContext, field);
      }
    }
  }

  /**
   * Finds rule violations in schema.
   *
   * @param schema
   *     to check
   * @return found violations
   */
  public List<RuleViolation> validate(Schema schema) {
    RuleContext ruleContext = new RuleContext();
    findViolations(ruleContext, schema);
    return ruleContext.getViolations();
  }
}
