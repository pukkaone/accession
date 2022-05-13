package com.github.pukkaone.accession.schema.registry.maven.rule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import lombok.Getter;

/**
 * Maintains path in object structure of field currently being validated so rule violation messages
 * can show it.
 */
public class RuleContext {

  private Deque<String> fieldPath = new ArrayDeque<>();

  @Getter
  private List<RuleViolation> violations = new ArrayList<>();

  /**
   * Appends field to path.
   *
   * @param fieldName
   *         to append
   */
  public void push(String fieldName) {
    fieldPath.addLast(fieldName);
  }

  /**
   * Removes last field in path.
   */
  public void pop() {
    fieldPath.removeLast();
  }

  private String getFieldPath() {
    return String.join(".", fieldPath);
  }

  /**
   * Adds rule violation for field identified by current path.
   *
   * @param rule
   *         violated rule
   * @param messageArguments
   *         arguments to format message
   */
  public void addViolation(Rule rule, Object... messageArguments) {
    violations.add(new RuleViolation(getFieldPath(), rule, messageArguments));
  }
}
