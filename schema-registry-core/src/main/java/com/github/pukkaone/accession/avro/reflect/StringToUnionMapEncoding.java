package com.github.pukkaone.accession.avro.reflect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.UnresolvedUnionException;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

/**
 * Encodes {@code Map<String, Object>} to Avro as a map from string to union of primitive values.
 */
public class StringToUnionMapEncoding extends AbstractEncoding<Map> {

  // These index values must correspond to the order of the union types in the constructor.
  private static final int NULL = 0;
  private static final int BOOLEAN = 1;
  private static final int BYTES = 2;
  private static final int DOUBLE = 3;
  private static final int INT = 4;
  private static final int LONG = 5;
  private static final int STRING = 6;

  private Schema valueSchema;

  /**
   * Constructor.
   */
  public StringToUnionMapEncoding() {
    valueSchema = Schema.createUnion(Arrays.asList(
        Schema.create(Schema.Type.NULL),
        Schema.create(Schema.Type.BOOLEAN),
        Schema.create(Schema.Type.BYTES),
        Schema.create(Schema.Type.DOUBLE),
        Schema.create(Schema.Type.INT),
        Schema.create(Schema.Type.LONG),
        Schema.create(Schema.Type.STRING)));

    schema = Schema.createMap(valueSchema);
    schema.addProp("java-class", Map.class.getName());
  }

  private void writeValue(Object value, Encoder out) throws IOException {
    if (value == null) {
      out.writeIndex(NULL);
      out.writeNull();
    } else if (value instanceof Boolean) {
      out.writeIndex(BOOLEAN);
      out.writeBoolean((Boolean) value);
    } else if (value instanceof byte[]) {
      out.writeIndex(BYTES);
      out.writeBytes((byte[]) value);
    } else if (value instanceof ByteBuffer) {
      out.writeIndex(BYTES);
      out.writeBytes((ByteBuffer) value);
    } else if (value instanceof Integer) {
      out.writeIndex(INT);
      out.writeInt((Integer) value);
    } else if (value instanceof Long) {
      out.writeIndex(LONG);
      out.writeLong((Long) value);
    } else if (value instanceof Number) {
      out.writeIndex(DOUBLE);
      out.writeDouble(((Number) value).doubleValue());
    } else {
      out.writeIndex(STRING);
      out.writeString(value.toString());
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void writeDatum(Object datum, Encoder out) throws IOException {
    Map<String, Object> map = (Map<String, Object>) datum;
    out.writeMapStart();
    out.setItemCount(map.size());
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      out.startItem();
      out.writeString(entry.getKey());
      writeValue(entry.getValue(), out);
    }

    out.writeMapEnd();
  }

  private Object readValue(Decoder in) throws IOException {
    Object value;
    int index = in.readIndex();
    switch (index) {
      case NULL:
        in.readNull();
        value = null;
        break;
      case BOOLEAN:
        value = in.readBoolean();
        break;
      case BYTES:
        value = in.readBytes(null);
        break;
      case DOUBLE:
        value = in.readDouble();
        break;
      case INT:
        value = in.readInt();
        break;
      case LONG:
        value = in.readLong();
        break;
      case STRING:
        value = in.readString();
        break;
      default:
        throw new UnresolvedUnionException(valueSchema, index);
    }

    return value;
  }

  @Override
  public Map<String, Object> readDatum(Object reuse, Decoder in) throws IOException {
    Map<String, Object> map = new HashMap<>();

    for (long n = in.readMapStart(); n != 0; n = in.mapNext()) {
      for (long i = 0; i < n; ++i) {
        String key = in.readString();
        Object value = readValue(in);
        map.put(key, value);
      }
    }

    return map;
  }
}
