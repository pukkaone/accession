package com.github.pukkaone.accession.avro.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.pukkaone.accession.persistence.Codeable;
import java.io.IOException;
import org.apache.avro.reflect.AvroEncode;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link NullableCodeableStringEncoding}.
 */
class NullableCodeableStringEncodingTest extends EncodingTestSupport {

  public enum Season implements Codeable<String> {
    SPRING("P"),
    SUMMER("U"),
    FALL("F"),
    WINTER("W");

    private String code;

    Season(String code) {
      this.code = code;
    }

    @Override
    public String getCode() {
      return code;
    }
  }

  public static class NullableSeasonEncoding extends NullableCodeableStringEncoding<Season> {
  }

  public static class Message {
    @AvroEncode(using = NullableSeasonEncoding.class)
    public Season season;
  }

  @Test
  void given_null_value_when_encode() throws IOException {
    Message originalMessage = new Message();

    Message message = encodeDecode(originalMessage);
    assertThat(message.season).isEqualTo(originalMessage.season);
  }

  @Test
  void given_present_value_when_encode() throws IOException {
    Message originalMessage = new Message();
    originalMessage.season = Season.SPRING;

    Message message = encodeDecode(originalMessage);
    assertThat(message.season).isEqualTo(originalMessage.season);
  }
}
