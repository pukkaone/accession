package com.example;

import com.github.pukkaone.accession.avro.reflect.NullableInstantEncoding;
import java.time.Instant;
import org.apache.avro.reflect.AvroEncode;

public class IncompatibleEvent {

  @AvroEncode(using = NullableInstantEncoding.class)
  private Instant updatedAt;
}
