package com.github.pukkaone.accession.schema.registry.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.pukkaone.accession.schema.registry.domain.Catalog;
import com.github.pukkaone.accession.schema.registry.domain.Registration;
import com.github.pukkaone.accession.schema.registry.function.SchemaUtils;
import com.github.pukkaone.accession.schema.registry.rest.dto.SchemaResponse;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Provides access to schema.
 */
@RequiredArgsConstructor
@Service
public class SchemaService {

  public static final Integer CATALOG_KEY = 0;

  private final LoadingCache<Integer, Catalog> catalogCache;

  private Catalog getCatalog() {
    return catalogCache.get(CATALOG_KEY);
  }

  /**
   * Gets all registered subjects.
   *
   * @return subjects
   */
  public Set<String> getSubjects() {
    return getCatalog()
        .getSubjectToVersionToRegistrationMap()
        .keySet();
  }

  /**
   * Finds versions by subject.
   *
   * @param subject
   *     subject to search for
   * @return versions
   */
  public Set<Integer> findBySubject(String subject) {
    return getCatalog()
        .getSubjectToVersionToRegistrationMap()
        .get(subject)
        .keySet();
  }

  /**
   * Finds registration by subject and version.
   *
   * @param subject
   *     subject to search for
   * @param version
   *     version to search for
   * @return versions
   */
  public SchemaResponse findBySubjectAndVersion(String subject, int version) {
    Registration registration = getCatalog()
        .getSubjectToVersionToRegistrationMap()
        .get(subject)
        .get(version);

    return SchemaResponse.builder()
        .id(registration.getSchemaId())
        .name(registration.getSubject())
        .version(registration.getVersion())
        .schema(registration.getSchema())
        .build();
  }

  /**
   * Finds latest registration by subject.
   *
   * @param subject
   *     subject to search for
   * @return registration
   */
  public SchemaResponse findLatestSchemaBySubject(String subject) {
    TreeMap<Integer, Registration> versionToRegistrationMap = getCatalog()
        .getSubjectToVersionToRegistrationMap()
        .get(subject);
    if (versionToRegistrationMap == null) {
      return null;
    }

    return findBySubjectAndVersion(subject, versionToRegistrationMap.lastKey());
  }

  /**
   * Finds schema by schema ID.
   *
   * @param schemaId
   *     schema ID to search for
   * @return schema
   */
  public SchemaResponse findBySchemaId(int schemaId) {
    Registration registration = getCatalog()
        .getIdToRegistrationMap()
        .get(schemaId);
    if (registration == null) {
      return null;
    }

    return SchemaResponse.builder()
        .schema(registration.getSchema())
        .build();
  }

  /**
   * Finds registration by subject and schema.
   *
   * @param subject
   *     subject to search for
   * @param schema
   *     schema to search for
   * @return schema ID
   */
  public Registration findBySubjectAndSchema(String subject, String schema) {
    Map<Long, Registration> fingerprintToRegistrationMap = getCatalog()
        .getSubjectToFingerprintToRegistrationMap()
        .get(subject);
    if (fingerprintToRegistrationMap == null) {
      return null;
    }

    return fingerprintToRegistrationMap.get(SchemaUtils.fingerprint(schema));
  }
}
