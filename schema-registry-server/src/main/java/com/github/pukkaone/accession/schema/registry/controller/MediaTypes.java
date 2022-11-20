package com.github.pukkaone.accession.schema.registry.controller;

/**
 * Producible media types.
 */
public final class MediaTypes {

  public static final String APPLICATION_SCHEMA_REGISTRY =
      "application/vnd.schemaregistry.v1+json";

  private MediaTypes() {
    throw new UnsupportedOperationException("Should not instantiate this class");
  }
}
