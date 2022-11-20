package com.github.pukkaone.accession.avro.reflect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares options for generating an Avro schema by reflection.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AvroReflect {

  /**
   * Whether non-primitive Java fields are nullable by default in generated Avro schema.
   *
   * @return whether non-primitive Java fields are nullable by default in generated Avro schema
   */
  boolean nullable() default false;
}
