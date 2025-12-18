package io.hedges.zoned.netty;

import io.hedges.zoned.core.dom.DnsWireReader;
import io.netty.buffer.ByteBuf;

final class NettyWireReader implements DnsWireReader {

    private final ByteBuf buf;

    NettyWireReader(ByteBuf buf) {
        if (buf == null) {
            throw new IllegalArgumentException("buf is null");
        }
        this.buf = buf;
    }

    @Override
    public int position() {
        return buf.readerIndex();
    }

    @Override
    public void position(int position) {
        buf.readerIndex(position);
    }

    @Override
    public int limit() {
        return buf.writerIndex();
    }

    @Override
    public int readU8() {
        return buf.readUnsignedByte();
    }

    @Override
    public int readU16() {
        return buf.readUnsignedShort();
    }

    @Override
    public long readU32() {
        return buf.readUnsignedInt();
    }

    @Override
    public void readBytes(byte[] dst, int offset, int length) {
        buf.readBytes(dst, offset, length);
    }

    @Override
    public int getU8(int index) {
        return buf.getUnsignedByte(index);
    }

    @Override
    public void getBytes(int index, byte[] dst, int offset, int length) {
        buf.getBytes(index, dst, offset, length);
    }
}
