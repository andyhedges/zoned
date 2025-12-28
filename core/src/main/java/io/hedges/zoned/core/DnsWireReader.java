// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core;

public interface DnsWireReader {

    int position();

    void position(int position);

    int limit();

    default int remaining() {
        return limit() - position();
    }

    int readU8();

    int readU16();

    long readU32();

    void readBytes(byte[] dst, int offset, int length);

    int getU8(int index);

    void getBytes(int index, byte[] dst, int offset, int length);
}
