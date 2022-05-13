package com.github.pukkaone.accession.schema.registry.maven;

import lombok.Getter;

/**
 * Action to perform when putting a version in the schema registry.
 */
public enum VersionAction {
  NONE(""),
  ADD("Add"),
  REPLACE("Replace");

  VersionAction(String message) {
    this.message = message;
  }

  @Getter
  private String message;
}
