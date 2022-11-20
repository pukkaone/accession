package com.github.pukkaone.accession.avro.reflect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;

/**
 * Implements common method for encoding tests.
 */
public class EncodingTestSupport {

  protected <T> T encodeDecode(T originalMessage) throws IOException {
    Schema schema = SchemaGenerator.getSchema(originalMessage.getClass());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BinaryEncoder encoder = EncoderFactory.get().directBinaryEncoder(out, null);
    ReflectDatumWriter<T> writer = new ReflectDatumWriter<>(schema);
    writer.write(originalMessage, encoder);
    encoder.flush();
    out.close();

    BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(out.toByteArray(), null);
    ReflectDatumReader<T> reader = new ReflectDatumReader<>(schema);
    return reader.read(null, decoder);
  }
}
