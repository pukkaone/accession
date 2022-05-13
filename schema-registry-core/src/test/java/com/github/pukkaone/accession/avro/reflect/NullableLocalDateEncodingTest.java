package com.github.pukkaone.accession.avro.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import org.apache.avro.reflect.AvroEncode;
import org.junit.jupiter.api.Test;

/**
 * {@link NullableLocalDateEncoding} tests.
 */
class NullableLocalDateEncodingTest extends EncodingTestSupport {

  public static class Message {
    @AvroEncode(using = NullableLocalDateEncoding.class)
    public LocalDate localDate;
  }

  @Test
  void given_null_value_when_encode() throws IOException {
    Message originalMessage = new Message();

    Message message = encodeDecode(originalMessage);
    assertThat(message.localDate).isEqualTo(originalMessage.localDate);
  }

  @Test
  void given_present_value_when_encode() throws IOException {
    Message originalMessage = new Message();
    originalMessage.localDate = LocalDate.now();

    Message message = encodeDecode(originalMessage);
    assertThat(message.localDate).isEqualTo(originalMessage.localDate);
  }
}
