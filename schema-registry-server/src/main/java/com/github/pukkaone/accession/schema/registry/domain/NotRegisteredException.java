package com.github.pukkaone.accession.schema.registry.domain;

/**
 * Thrown on attempt to register a schema that is not already registered.
 */
public class NotRegisteredException extends RuntimeException {

  /**
   * Constructor.
   *
   * @param message
   *     detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public NotRegisteredException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message
   *     detail message. The detail message is saved for later retrieval by the getMessage() method.
   * @param cause
   *     cause. The cause is saved for later retrieval by the getCause() method. A null value is
   *     permitted, and indicates that the cause is nonexistent or unknown.
   */
  public NotRegisteredException(String message, Throwable cause) {
    super(message, cause);
  }
}
