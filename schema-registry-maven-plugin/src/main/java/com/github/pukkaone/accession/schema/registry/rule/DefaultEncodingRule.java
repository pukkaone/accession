package com.github.pukkaone.accession.schema.registry.rule;

import com.google.auto.service.AutoService;
import org.apache.avro.Schema;

/**
 * The schema ReflectData generated from a Java class does not encode the fields.
 */
@AutoService(Rule.class)
public class DefaultEncodingRule implements Rule {

  @Override
  public void findViolations(RuleContext ruleContext, Schema.Field field) {
    var schema = Rules.getSchemaFromNullable(field.schema());
    if (schema.getType() == Schema.Type.RECORD && schema.getFields().isEmpty()) {
      ruleContext.addViolation(this, schema.getNamespace(), schema.getName());
    }
  }
}
