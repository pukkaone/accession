package com.github.pukkaone.accession.schema.registry.rule;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Rule violation.
 */
@AllArgsConstructor
@Data
public class RuleViolation {

  private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("rule-messages");

  private String fieldPath;
  private Rule rule;
  private Object[] messageArguments;

  @Override
  public String toString() {
    StringBuilder message = new StringBuilder();
    message.append("Field ").append(fieldPath).append(": ");
    String pattern = MESSAGES.getString(rule.getClass().getSimpleName());
    message.append(MessageFormat.format(pattern, messageArguments));
    return message.toString();
  }
}
