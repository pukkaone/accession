package com.github.pukkaone.accession.avro.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Instant;
import org.apache.avro.reflect.AvroEncode;
import org.junit.jupiter.api.Test;

/**
 * {@link NullableInstantEncoding} tests.
 */
class NullableInstantEncodingTest extends EncodingTestSupport {

  public static class Message {
    @AvroEncode(using = NullableInstantEncoding.class)
    public Instant instant;
  }

  @Test
  void given_null_value_when_encode() throws IOException {
    Message originalMessage = new Message();

    Message message = encodeDecode(originalMessage);
    assertThat(message.instant).isEqualTo(originalMessage.instant);
  }

  @Test
  void given_present_value_when_encode() throws IOException {
    Message originalMessage = new Message();
    originalMessage.instant = Instant.now();

    Message message = encodeDecode(originalMessage);
    assertThat(message.instant).isEqualTo(originalMessage.instant);
  }
}
