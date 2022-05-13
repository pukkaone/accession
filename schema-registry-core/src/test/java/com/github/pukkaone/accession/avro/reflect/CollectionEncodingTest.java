package com.github.pukkaone.accession.avro.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.pukkaone.accession.persistence.Codeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.avro.reflect.AvroEncode;
import org.junit.jupiter.api.Test;

/**
 * Tests encoding collection of enum.
 */
class CollectionEncodingTest extends EncodingTestSupport {

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

  public static class SeasonListEncoding extends CodeableStringListEncoding<Season> {
  }

  public static class SeasonSetEncoding extends CodeableStringSetEncoding<Season> {
  }

  public static class NullableSeasonListEncoding
      extends NullableCodeableStringListEncoding<Season> {
  }

  public static class NullableSeasonSetEncoding extends NullableCodeableStringSetEncoding<Season> {
  }

  public static class Message {
    @AvroEncode(using = SeasonListEncoding.class)
    public List<Season> seasonList;

    @AvroEncode(using = SeasonSetEncoding.class)
    public Set<Season> seasonSet;
  }

  @AvroReflect(nullable = true)
  public static class NullableMessage {
    @AvroEncode(using = NullableSeasonListEncoding.class)
    public List<Season> seasonList;

    @AvroEncode(using = NullableSeasonSetEncoding.class)
    public Set<Season> seasonSet;
  }

  @Test
  void given_not_nullable_field_when_encode_list() throws IOException {
    Message originalMessage = new Message();
    originalMessage.seasonList = Arrays.asList(Season.SPRING, Season.SUMMER);

    Message message = encodeDecode(originalMessage);
    assertThat(message.seasonList).isEqualTo(originalMessage.seasonList);
  }

  @Test
  void given_not_nullable_field_when_encode_set() throws IOException {
    Message originalMessage = new Message();
    originalMessage.seasonSet = new HashSet<>(Arrays.asList(Season.FALL, Season.WINTER));

    Message message = encodeDecode(originalMessage);
    assertThat(message.seasonSet).isEqualTo(originalMessage.seasonSet);
  }

  @Test
  void given_nullable_field_when_encode_list() throws IOException {
    NullableMessage originalMessage = new NullableMessage();
    originalMessage.seasonList = Arrays.asList(Season.SPRING, Season.SUMMER);

    NullableMessage message = encodeDecode(originalMessage);
    assertThat(message.seasonList).isEqualTo(originalMessage.seasonList);
  }

  @Test
  void given_nullable_field_when_encode_set() throws IOException {
    NullableMessage originalMessage = new NullableMessage();
    originalMessage.seasonSet = new HashSet<>(Arrays.asList(Season.FALL, Season.WINTER));

    NullableMessage message = encodeDecode(originalMessage);
    assertThat(message.seasonSet).isEqualTo(originalMessage.seasonSet);
  }
}
