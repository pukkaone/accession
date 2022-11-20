package com.example;

import com.github.pukkaone.accession.avro.reflect.NullableInstantEncoding;
import java.time.Instant;
import org.apache.avro.reflect.AvroEncode;

public class CompatibleEvent {

  @AvroEncode(using = NullableInstantEncoding.class)
  private Instant updatedAt;

  private String updatedBy;

  private String createdBy;
}
