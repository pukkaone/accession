package com.github.pukkaone.accession.schema.registry.rule;

import com.google.auto.service.AutoService;
import java.util.Set;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificData;

/**
 * Java class must not be {@link java.util.Set}.
 */
@AutoService(Rule.class)
public class JavaClassSetRule implements Rule {

  @Override
  public void findViolations(RuleContext ruleContext, Schema.Field field) {
    Schema schema = Rules.getSchemaFromNullable(field.schema());
    String javaClass = schema.getProp(SpecificData.CLASS_PROP);
    if (Set.class.getName().equals(javaClass)) {
      ruleContext.addViolation(this);
    }
  }
}
