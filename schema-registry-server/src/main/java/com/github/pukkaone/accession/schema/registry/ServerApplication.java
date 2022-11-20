package com.github.pukkaone.accession.schema.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.ssh.HostKeyAlgoSupported;

/**
 * Spring Boot application entry point.
 */
@SpringBootApplication(scanBasePackageClasses = HostKeyAlgoSupported.class)
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
