package com.github.pukkaone.accession.avro.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDateTime;
import org.apache.avro.reflect.AvroEncode;
import org.junit.jupiter.api.Test;

/**
 * {@link NullableLocalDateTimeEncoding} tests.
 */
class NullableLocalDateTimeEncodingTest extends EncodingTestSupport {

  public static class Message {
    @AvroEncode(using = NullableLocalDateTimeEncoding.class)
    public LocalDateTime localDateTime;
  }

  @Test
  void given_null_value_when_encode() throws IOException {
    Message originalMessage = new Message();

    Message message = encodeDecode(originalMessage);
    assertThat(message.localDateTime).isEqualTo(originalMessage.localDateTime);
  }

  @Test
  void given_present_value_when_encode() throws IOException {
    Message originalMessage = new Message();
    originalMessage.localDateTime = LocalDateTime.now();

    Message message = encodeDecode(originalMessage);
    assertThat(message.localDateTime).isEqualTo(originalMessage.localDateTime);
  }
}
