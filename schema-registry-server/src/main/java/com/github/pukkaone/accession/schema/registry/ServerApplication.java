package com.github.pukkaone.accession.schema.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point.
 */
@SpringBootApplication
public class ServerApplication {

  /**
   * Spring Boot application entry point.
   *
   * @param args
   *     command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(ServerApplication.class, args);
  }
}
