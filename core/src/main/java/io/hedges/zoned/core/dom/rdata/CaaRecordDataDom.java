// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Domain model for DNS CAA record RDATA.
 *
 * <p>RDATA is flags (8-bit), tag length (8-bit), tag (ASCII), and value bytes.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Flags     | 1            | Property flags.            |
 * | TagLength | 1            | Length of tag.             |
 * | Tag       | variable     | ASCII tag.                 |
 * | Value     | variable     | Value bytes.               |
 * +-----------+--------------+----------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class CaaRecordDataDom implements RDataDom {
    private int flags;
    private String tag;
    private byte[] value;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length < 2) {
            throw new IllegalArgumentException("CAA RDATA requires flags and tag length");
        }
        int flags = RDataUtils.readU8(rdata, 0);
        int tagLength = RDataUtils.readU8(rdata, 1);
        if (tagLength == 0) {
            throw new IllegalArgumentException("CAA tag length must be at least 1");
        }
        if (tagLength > 15) {
            throw new IllegalArgumentException("CAA tag length must be between 1 and 15");
        }
        int tagStart = 2;
        int tagEnd = tagStart + tagLength;
        if (tagEnd > rdata.length) {
            throw new IllegalArgumentException("CAA tag length exceeds RDATA length");
        }
        byte[] tagBytes = Arrays.copyOfRange(rdata, tagStart, tagEnd);
        validateTagBytes(tagBytes);
        String tag = new String(tagBytes, StandardCharsets.US_ASCII);
        byte[] value = Arrays.copyOfRange(rdata, tagEnd, rdata.length);
        return CaaRecordDataDom.builder()
                .flags(flags)
                .tag(tag)
                .value(value)
                .build();
    }

    @Override
    public byte[] to() {
        if (flags < 0 || flags > 0xFF) {
            throw new IllegalArgumentException("CAA flags must be between 0 and 255");
        }
        if (tag == null) {
            throw new IllegalArgumentException("CAA tag is null");
        }
        byte[] tagBytes = tag.getBytes(StandardCharsets.US_ASCII);
        validateTag(tag, tagBytes);
        if (value == null) {
            throw new IllegalArgumentException("CAA value is null");
        }
        if (tagBytes.length > 0xFF) {
            throw new IllegalArgumentException("CAA tag exceeds 255 bytes");
        }
        byte[] out = new byte[2 + tagBytes.length + value.length];
        out[0] = (byte) (flags & 0xFF);
        out[1] = (byte) (tagBytes.length & 0xFF);
        System.arraycopy(tagBytes, 0, out, 2, tagBytes.length);
        System.arraycopy(value, 0, out, 2 + tagBytes.length, value.length);
        return out;
    }

    private static void validateTag(String tag, byte[] tagBytes) {
        if (tag.isEmpty()) {
            throw new IllegalArgumentException("CAA tag must not be empty");
        }
        if (tagBytes.length > 15) {
            throw new IllegalArgumentException("CAA tag length must be between 1 and 15");
        }
        validateTagBytes(tagBytes);
    }

    private static void validateTagBytes(byte[] tagBytes) {
        for (byte b : tagBytes) {
            int ch = b & 0xFF;
            if (ch > 0x7F) {
                throw new IllegalArgumentException("CAA tag must be US-ASCII");
            }
            boolean ok = (ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9')
                    || ch == '-';
            if (!ok) {
                throw new IllegalArgumentException("CAA tag contains invalid characters");
            }
        }
    }
}
