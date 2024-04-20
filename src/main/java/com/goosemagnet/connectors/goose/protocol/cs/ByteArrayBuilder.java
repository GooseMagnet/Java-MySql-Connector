package com.goosemagnet.connectors.goose.protocol.cs;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;

@Value
@AllArgsConstructor
public class ByteArrayBuilder {

    DataOutputStream dos;
    ByteArrayOutputStream baos;

    public ByteArrayBuilder(ByteArrayOutputStream baos) {
        this(new DataOutputStream(baos), baos);
    }

    public ByteArrayBuilder() {
        this(new ByteArrayOutputStream());
    }

    @SneakyThrows
    public ByteArrayBuilder putInt1(Types.Int1 int1) {
        putNum(1, int1.getValue());
        return this;
    }

    @SneakyThrows
    public ByteArrayBuilder putInt2(Types.Int2 int2) {
        putNum(2, int2.getValue());
        return this;
    }

    @SneakyThrows
    public ByteArrayBuilder putInt3(Types.Int3 int3) {
        putNum(3, int3.getValue());
        return this;
    }

    @SneakyThrows
    public ByteArrayBuilder putInt4(Types.Int4 int4) {
        putNum(4, int4.getValue());
        return this;
    }

    @SneakyThrows
    public ByteArrayBuilder putNullTerminatedString(Types.NullTerminatedString nts) {
        putBytes(nts.getValue().getBytes(StandardCharsets.UTF_8));
        dos.writeByte(0);
        return this;
    }

    @SneakyThrows
    public ByteArrayBuilder putByte(byte value) {
        dos.writeByte(value);
        return this;
    }

    @SneakyThrows
    public ByteArrayBuilder putBytes(byte[] bytes) {
        for (byte b : bytes) {
            dos.writeByte(b);
        }
        return this;
    }

    public byte[] toByteArray() {
        return baos.toByteArray();
    }

    @SneakyThrows
    private void putNum(int bytes, long value) {
        for (int i = 0; i < bytes; ++i) {
            dos.writeByte((byte) (value >> (i * 8)) & 0xFF);
        }
    }
}
