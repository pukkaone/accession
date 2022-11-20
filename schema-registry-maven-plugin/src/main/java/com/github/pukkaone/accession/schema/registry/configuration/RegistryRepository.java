package com.github.pukkaone.accession.schema.registry.configuration;

import lombok.Data;

/**
 * From a project Git branch, determine a schema registry Git repository and branch.
 */
@Data
public class RegistryRepository {

  private String projectBranchPattern;
  private String registryUri;
  private String registryBranch;
  private boolean mutableVersions;
}
