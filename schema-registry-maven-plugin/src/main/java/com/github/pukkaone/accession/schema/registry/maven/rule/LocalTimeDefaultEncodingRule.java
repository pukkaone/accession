package com.github.pukkaone.accession.schema.registry.maven.rule;

import com.google.auto.service.AutoService;
import java.time.LocalTime;
import org.apache.avro.Schema;

/**
 * {@link LocalTime} encoding must not be default implementation.
 */
@AutoService(Rule.class)
public class LocalTimeDefaultEncodingRule implements Rule {

  @Override
  public void findViolations(RuleContext ruleContext, Schema.Field field) {
    if (Rules.isDefaultEncoding(field.schema(), LocalTime.class)) {
      ruleContext.addViolation(this);
    }
  }
}
