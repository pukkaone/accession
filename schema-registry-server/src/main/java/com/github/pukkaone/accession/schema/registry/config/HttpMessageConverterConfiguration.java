package com.github.pukkaone.accession.schema.registry.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Excludes {@code ;charset=UTF-8} from the {@code Content-Type} response header value.
 */
@Configuration
public class HttpMessageConverterConfiguration {

  @Bean
  public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
      ObjectMapper objectMapper) {

    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(objectMapper);
    converter.setDefaultCharset(null);
    return converter;
  }
}
