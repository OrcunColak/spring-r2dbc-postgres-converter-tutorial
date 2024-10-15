package com.colak.springtutorial.config;

import io.r2dbc.spi.Blob;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Configuration
public class R2DBCConfiguration {

}

@ReadingConverter
class ByteArrayToByteBufferConverter implements Converter<byte[], ByteBuffer> {

    @Override
    public ByteBuffer convert(byte[] source) {
        return ByteBuffer.wrap(source);
    }
}

@WritingConverter
class ByteBufferToByteArrayConverter implements Converter<ByteBuffer, byte[]> {

    @Override
    public byte[] convert(ByteBuffer source) {
        return source.array();
    }
}

@ReadingConverter
class ByteArrayToBlobConverter implements Converter<byte[], Blob> {

    @Override
    public Blob convert(byte[] source) {
        return Blob.from(Mono.just(ByteBuffer.wrap(source)));
    }
}

