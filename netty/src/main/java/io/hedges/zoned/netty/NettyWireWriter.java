package io.hedges.zoned.netty;

import io.hedges.zoned.core.dom.DnsWireWriter;
import io.netty.buffer.ByteBuf;

final class NettyWireWriter implements DnsWireWriter {

    private final ByteBuf buf;

    NettyWireWriter(ByteBuf buf) {
        if (buf == null) {
            throw new IllegalArgumentException("buf is null");
        }
        this.buf = buf;
    }

    @Override
    public int position() {
        return buf.writerIndex();
    }

    @Override
    public void writeU8(int value) {
        buf.writeByte(value);
    }

    @Override
    public void writeU16(int value) {
        buf.writeShort(value);
    }

    @Override
    public void writeU32(long value) {
        buf.writeInt((int) value);
    }

    @Override
    public void writeBytes(byte[] src, int offset, int length) {
        buf.writeBytes(src, offset, length);
    }
}
