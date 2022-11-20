package com.github.pukkaone.accession.schema.registry.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;

/**
 * Collection of subjects and schemas.
 */
@Getter
public class Catalog {

  private Map<String, Map<Long, Registration>> subjectToFingerprintToRegistrationMap =
      new HashMap<>();
  private Map<String, TreeMap<Integer, Registration>> subjectToVersionToRegistrationMap =
      new TreeMap<>();
  private Map<Integer, Registration> idToRegistrationMap = new HashMap<>();
}
