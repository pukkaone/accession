package com.github.pukkaone.accession.schema.registry.maven;

import com.github.pukkaone.accession.avro.reflect.SchemaGenerator;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.avro.Schema;

/**
 * Generates and writes schema.
 */
public class SchemaBuilder {

  private ClassLoader classLoader;

  /**
   * Constructor.
   *
   * @param classpathElements
   *     classpath for classes to introspect
   */
  public SchemaBuilder(List<String> classpathElements) {
    classLoader = new URLClassLoader(
        toURLs(classpathElements),
        Thread.currentThread().getContextClassLoader());
  }

  private static URL[] toURLs(List<String> classpathElements) {
    return classpathElements.stream().distinct().map(element -> {
      try {
        return new File(element).toURI().toURL();
      } catch (MalformedURLException e) {
        throw new IllegalStateException("Cannot convert class path element to URL", e);
      }
    }).toArray(URL[]::new);
  }

  /**
   * Generates schema.
   *
   * @param registration
   *     registration request
   * @return schema
   */
  public Schema generateSchema(Registration registration) {
    Class<?> reflectClass;
    try {
      reflectClass = classLoader.loadClass(registration.getReflectClass());
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Class not found " + registration.getReflectClass(), e);
    }

    return SchemaGenerator.getSchema(reflectClass);
  }

  /**
   * Reads schema from file.
   *
   * @param schemaFile
   *     file to read
   * @return schema, or null if file not found
   */
  public static Schema readSchemaFile(Path schemaFile) {
    if (!Files.exists(schemaFile)) {
      return null;
    }

    try {
      return new Schema.Parser().parse(schemaFile.toFile());
    } catch (IOException e) {
      throw new IllegalStateException("Cannot write file " + schemaFile, e);
    }
  }

  /**
   * Writes schema to file.
   *
   * @param schema
   *     schema to write
   * @param schemaFile
   *     destination file
   */
  public static void writeSchemaFile(Schema schema, Path schemaFile) {
    String schemaString = schema.toString(true) + System.lineSeparator();
    try {
      Files.writeString(schemaFile, schemaString);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot write file " + schemaFile, e);
    }
  }
}
