package com.github.pukkaone.accession.schema.registry.rule;

import com.google.auto.service.AutoService;
import java.time.LocalDateTime;
import org.apache.avro.Schema;

/**
 * {@link LocalDateTime} encoding must not be default implementation.
 */
@AutoService(Rule.class)
public class LocalDateTimeDefaultEncodingRule implements Rule {

  @Override
  public void findViolations(RuleContext ruleContext, Schema.Field field) {
    if (Rules.isDefaultEncoding(field.schema(), LocalDateTime.class)) {
      ruleContext.addViolation(this);
    }
  }
}
