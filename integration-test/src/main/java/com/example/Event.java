package com.example;

import com.github.pukkaone.accession.avro.reflect.NullableInstantEncoding;
import java.time.Instant;
import org.apache.avro.reflect.AvroAlias;
import org.apache.avro.reflect.AvroEncode;

@AvroAlias(space = "com.example", alias = "CompatibleEvent")
@AvroAlias(space = "com.example", alias = "IncompatibleEvent")
public class Event {

  @AvroEncode(using = NullableInstantEncoding.class)
  private Instant updatedAt;

  private String updatedBy;
}
