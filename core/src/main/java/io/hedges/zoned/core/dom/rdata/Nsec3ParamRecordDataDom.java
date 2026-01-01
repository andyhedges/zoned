// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS NSEC3PARAM record RDATA.
 *
 * <p>RDATA is hash algorithm (8-bit), flags (8-bit), iterations (16-bit),
 * salt length (8-bit), and salt.</p>
 
 *
 * <pre>
 * +-------------+--------------+----------------------------+
 * | Field       | Size (octets)| Description                |
 * +-------------+--------------+----------------------------+
 * | HashAlg     | 1            | Hash algorithm.            |
 * | Flags       | 1            | NSEC3 flags.               |
 * | Iterations  | 2            | Iteration count.           |
 * | SaltLength  | 1            | Salt length.               |
 * | Salt        | variable     | Salt bytes.                |
 * +-------------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes parameters (hash algorithm, iterations, salt) used for NSEC3 denial of existence.</p>
 * <p>This record signals how NSEC3 records are generated so validators can compute and verify hashed
 * owner names consistently across the zone.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc5155">RFC 5155</a>.</p>*/
@Getter
@Builder
@ToString
public class Nsec3ParamRecordDataDom implements RDataDom {
    private int hashAlgorithm;
    private int flags;
    private int iterations;
    private byte[] salt;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length < 5) {
            throw new IllegalArgumentException("NSEC3PARAM RDATA requires hash algorithm, flags, iterations, and salt");
        }
        int hashAlgorithm = RDataUtils.readU8(rdata, 0);
        int flags = RDataUtils.readU8(rdata, 1);
        int iterations = RDataUtils.readU16(rdata, 2);
        int saltLength = RDataUtils.readU8(rdata, 4);
        int expectedLength = 5 + saltLength;
        if (expectedLength != rdata.length) {
            throw new IllegalArgumentException("NSEC3PARAM salt length does not match RDATA length");
        }
        byte[] salt = new byte[saltLength];
        if (saltLength > 0) {
            System.arraycopy(rdata, 5, salt, 0, saltLength);
        }
        return Nsec3ParamRecordDataDom.builder()
                                      .hashAlgorithm(hashAlgorithm)
                                      .flags(flags)
                                      .iterations(iterations)
                                      .salt(salt)
                                      .build();
    }

    @Override
    public byte[] to() {
        if (hashAlgorithm < 0 || hashAlgorithm > 0xFF) {
            throw new IllegalArgumentException("NSEC3PARAM hash algorithm must be between 0 and 255");
        }
        if (flags < 0 || flags > 0xFF) {
            throw new IllegalArgumentException("NSEC3PARAM flags must be between 0 and 255");
        }
        if (iterations < 0 || iterations > 0xFFFF) {
            throw new IllegalArgumentException("NSEC3PARAM iterations must be between 0 and 65535");
        }
        if (salt == null) {
            throw new IllegalArgumentException("NSEC3PARAM salt is null");
        }
        if (salt.length > 0xFF) {
            throw new IllegalArgumentException("NSEC3PARAM salt length must be between 0 and 255");
        }
        byte[] out = new byte[5 + salt.length];
        out[0] = (byte) (hashAlgorithm & 0xFF);
        out[1] = (byte) (flags & 0xFF);
        out[2] = (byte) ((iterations >> 8) & 0xFF);
        out[3] = (byte) (iterations & 0xFF);
        out[4] = (byte) (salt.length & 0xFF);
        if (salt.length > 0) {
            System.arraycopy(salt, 0, out, 5, salt.length);
        }
        return out;
    }
}
