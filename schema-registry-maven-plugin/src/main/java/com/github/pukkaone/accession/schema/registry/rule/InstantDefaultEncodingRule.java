package com.github.pukkaone.accession.schema.registry.rule;

import com.google.auto.service.AutoService;
import java.time.Instant;
import org.apache.avro.Schema;

/**
 * {@link Instant} encoding must not be default implementation.
 */
@AutoService(Rule.class)
public class InstantDefaultEncodingRule implements Rule {

  @Override
  public void findViolations(RuleContext ruleContext, Schema.Field field) {
    if (Rules.isDefaultEncoding(field.schema(), Instant.class)) {
      ruleContext.addViolation(this);
    }
  }
}
