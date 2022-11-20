package com.github.pukkaone.accession.schema.registry.config;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.pukkaone.accession.schema.registry.domain.Catalog;
import com.github.pukkaone.accession.schema.registry.repository.CatalogRepository;
import com.github.pukkaone.accession.schema.registry.service.SchemaService;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures cache of subjects and schemas.
 */
@Configuration
public class CatalogCacheConfiguration {

  @Bean
  public LoadingCache<Integer, Catalog> catalogCache(
      SchemaRegistryConfigurationProperties properties,
      CatalogRepository catalogRepository) {

    LoadingCache<Integer, Catalog> cache = Caffeine.newBuilder()
        .refreshAfterWrite(properties.getRefreshSeconds(), TimeUnit.SECONDS)
        .build(new CacheLoader<Integer, Catalog>() {
          @Override
          public Catalog load(Integer key) throws Exception {
            return catalogRepository.readCatalog();
          }

          @Override
          public Catalog reload(Integer key, Catalog oldValue) {
            if (!catalogRepository.pull()) {
              return oldValue;
            }

            return catalogRepository.readCatalog();
          }
        });
    cache.get(SchemaService.CATALOG_KEY);
    return cache;
  }
}
