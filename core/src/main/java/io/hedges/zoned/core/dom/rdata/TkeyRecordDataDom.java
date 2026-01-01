// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS TKEY record RDATA.
 *
 * <p>RDATA is algorithm name, inception time, expiration time, mode,
 * error, key data, and other data.</p>
 *
 * <pre>
 * +--------------+--------------+-------------------------------------------+
 * | Field        | Size (octets)| Description                               |
 * +--------------+--------------+-------------------------------------------+
 * | Algorithm    | variable     | Algorithm name (wire format).             |
 * | Inception    | 4            | Start time (seconds since epoch).         |
 * | Expiration   | 4            | End time (seconds since epoch).           |
 * | Mode         | 2            | TKEY mode.                                |
 * | Error        | 2            | Extended error code.                      |
 * | KeyLength    | 2            | Length of key data in octets.             |
 * | KeyData      | variable     | Key material.                             |
 * | OtherLength  | 2            | Length of other data in octets.           |
 * | OtherData    | variable     | Other data bytes.                         |
 * +--------------+--------------+-------------------------------------------+
 * </pre>
 
 * <p>Purpose: Provides Transaction Key (TKEY) material for establishing TSIG keys.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc2930">RFC 2930</a>.</p>*/
@Getter
@Builder
@ToString
public class TkeyRecordDataDom implements RDataDom {
    private DnsNameDom algorithm;
    private long inception;
    private long expiration;
    private int mode;
    private int error;
    private byte[] keyData;
    private byte[] otherData;

    public static RDataDom from(byte[] rdata) {
        return from(rdata, null);
    }

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        if (rdata == null) {
            throw new IllegalArgumentException("TKEY RDATA cannot be null");
        }
        RDataUtils.DnsNameParseResult algorithmResult = RDataUtils.parseDnsName(rdata, 0, resolver);
        int idx = algorithmResult.nextIndex();
        if (rdata.length - idx < 16) {
            throw new IllegalArgumentException("TKEY RDATA is too short");
        }

        long inception = RDataUtils.readU32(rdata, idx);
        idx += 4;
        long expiration = RDataUtils.readU32(rdata, idx);
        idx += 4;
        int mode = RDataUtils.readU16(rdata, idx);
        idx += 2;
        int error = RDataUtils.readU16(rdata, idx);
        idx += 2;
        int keyLength = RDataUtils.readU16(rdata, idx);
        idx += 2;

        if (idx + keyLength + 2 > rdata.length) {
            throw new IllegalArgumentException("TKEY key data length exceeds RDATA bounds");
        }
        byte[] keyData = new byte[keyLength];
        System.arraycopy(rdata, idx, keyData, 0, keyLength);
        idx += keyLength;

        int otherLength = RDataUtils.readU16(rdata, idx);
        idx += 2;
        if (idx + otherLength > rdata.length) {
            throw new IllegalArgumentException("TKEY other data length exceeds RDATA bounds");
        }
        if (idx + otherLength != rdata.length) {
            throw new IllegalArgumentException("Extra bytes after TKEY other data");
        }
        byte[] otherData = new byte[otherLength];
        System.arraycopy(rdata, idx, otherData, 0, otherLength);

        return TkeyRecordDataDom.builder()
                .algorithm(algorithmResult.name())
                .inception(inception)
                .expiration(expiration)
                .mode(mode)
                .error(error)
                .keyData(keyData)
                .otherData(otherData)
                .build();
    }

    @Override
    public byte[] to() {
        if (algorithm == null) {
            throw new IllegalArgumentException("TKEY RDATA requires algorithm name");
        }
        validateU32(inception, "inception");
        validateU32(expiration, "expiration");
        if (mode < 0 || mode > 0xFFFF) {
            throw new IllegalArgumentException("TKEY mode must be between 0 and 65535");
        }
        if (error < 0 || error > 0xFFFF) {
            throw new IllegalArgumentException("TKEY error must be between 0 and 65535");
        }
        if (keyData == null) {
            throw new IllegalArgumentException("TKEY key data must not be null");
        }
        if (otherData == null) {
            throw new IllegalArgumentException("TKEY other data must not be null");
        }
        if (keyData.length > 0xFFFF) {
            throw new IllegalArgumentException("TKEY key data length must be between 0 and 65535");
        }
        if (otherData.length > 0xFFFF) {
            throw new IllegalArgumentException("TKEY other data length must be between 0 and 65535");
        }

        byte[] algorithmBytes = RDataUtils.toByteArray(algorithm);
        byte[] out = new byte[algorithmBytes.length + 16 + keyData.length + otherData.length];
        int idx = 0;
        System.arraycopy(algorithmBytes, 0, out, idx, algorithmBytes.length);
        idx += algorithmBytes.length;
        RDataUtils.writeU32(out, idx, inception);
        idx += 4;
        RDataUtils.writeU32(out, idx, expiration);
        idx += 4;
        out[idx++] = (byte) ((mode >> 8) & 0xFF);
        out[idx++] = (byte) (mode & 0xFF);
        out[idx++] = (byte) ((error >> 8) & 0xFF);
        out[idx++] = (byte) (error & 0xFF);
        out[idx++] = (byte) ((keyData.length >> 8) & 0xFF);
        out[idx++] = (byte) (keyData.length & 0xFF);
        System.arraycopy(keyData, 0, out, idx, keyData.length);
        idx += keyData.length;
        out[idx++] = (byte) ((otherData.length >> 8) & 0xFF);
        out[idx++] = (byte) (otherData.length & 0xFF);
        System.arraycopy(otherData, 0, out, idx, otherData.length);
        return out;
    }

    private static void validateU32(long value, String field) {
        if (value < 0 || value > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("TKEY " + field + " must be between 0 and 4294967295");
        }
    }
}
