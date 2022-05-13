package com.github.pukkaone.accession.schema.registry.maven.rule;

import com.google.auto.service.AutoService;
import java.time.LocalDate;
import org.apache.avro.Schema;

/**
 * {@link LocalDate} encoding must not be default implementation.
 */
@AutoService(Rule.class)
public class LocalDateDefaultEncodingRule implements Rule {

  @Override
  public void findViolations(RuleContext ruleContext, Schema.Field field) {
    if (Rules.isDefaultEncoding(field.schema(), LocalDate.class)) {
      ruleContext.addViolation(this);
    }
  }
}
