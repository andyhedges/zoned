// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core;

public interface DnsWireWriter {

    int position();

    void writeU8(int value);

    void writeU16(int value);

    void writeU32(long value);

    void writeBytes(byte[] src, int offset, int length);

    default void writeBytes(byte[] src) {
        writeBytes(src, 0, src.length);
    }
}
