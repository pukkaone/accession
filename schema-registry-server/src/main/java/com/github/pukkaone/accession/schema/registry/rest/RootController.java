package com.github.pukkaone.accession.schema.registry.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint that does nothing and can be used to check for liveness.
 */
@RequestMapping(value = "/", produces = MediaTypes.APPLICATION_SCHEMA_REGISTRY)
@RestController
public class RootController {

  /**
   * Gets empty object.
   *
   * @return object
   */
  @RequestMapping(method = RequestMethod.GET)
  public String get() {
    return "{}";
  }
}
