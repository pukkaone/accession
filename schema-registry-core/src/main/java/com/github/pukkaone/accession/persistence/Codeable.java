package com.github.pukkaone.accession.persistence;

/**
 * Provides a code to represent an enum constant in a data store or message protocol. Storing a code
 * instead of the enum constant name allows us to rename the enum constant without having to worry
 * about migrating values previously stored with the old name.
 *
 * @param <C>
 *     code representation type
 */
public interface Codeable<C> {

  /**
   * Gets code representing an enum constant.
   *
   * @return code
   */
  C getCode();
}
