package com.github.pukkaone.accession.schema.registry.rule;

import org.apache.avro.Schema;

/**
 * Rule to apply to an Avro field.
 */
public interface Rule {

  /**
   * Finds violations of this rule in a field.
   *
   * @param ruleContext
   *     rule context
   * @param field
   *     to check
   */
  void findViolations(RuleContext ruleContext, Schema.Field field);
}
