package com.github.pukkaone.accession.avro.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalTime;
import org.apache.avro.reflect.AvroEncode;
import org.junit.jupiter.api.Test;

/**
 * {@link NullableLocalTimeEncoding} tests.
 */
class NullableLocalTimeEncodingTest extends EncodingTestSupport {

  public static class Message {
    @AvroEncode(using = NullableLocalTimeEncoding.class)
    public LocalTime localTime;
  }

  @Test
  void given_null_value_when_encode() throws IOException {
    Message originalMessage = new Message();

    Message message = encodeDecode(originalMessage);
    assertThat(message.localTime).isEqualTo(originalMessage.localTime);
  }

  @Test
  void given_present_value_when_encode() throws IOException {
    Message originalMessage = new Message();
    originalMessage.localTime = LocalTime.now();

    Message message = encodeDecode(originalMessage);
    assertThat(message.localTime).isEqualTo(originalMessage.localTime);
  }
}
