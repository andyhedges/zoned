// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS NSEC3 record RDATA.
 *
 * <p>RDATA is hash algorithm (8-bit), flags (8-bit), iterations (16-bit),
 * salt length (8-bit), salt, hash length (8-bit), next hashed owner name,
 * and type bitmaps.</p>
 
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
 * | HashLength  | 1            | Hash length.               |
 * | NextOwner   | variable     | Next hashed owner.         |
 * | TypeMap     | variable     | Type bitmaps.              |
 * +-------------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Provides DNSSEC authenticated denial of existence using hashed names to reduce zone enumeration.</p>
 * <p>NSEC3 uses hashing, salt, and iterations to obscure owner names while still proving non-existence,
 * and can optionally use opt-out to reduce signed data for unsigned delegations.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc5155">RFC 5155</a>.</p>*/
@Getter
@Builder
@ToString
public class Nsec3RecordDataDom implements RDataDom {
    private int hashAlgorithm;
    private int flags;
    private int iterations;
    private byte[] salt;
    private byte[] nextHashedOwner;
    private byte[] typeBitmaps;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length < 6) {
            throw new IllegalArgumentException("NSEC3 RDATA requires hash algorithm, flags, iterations, salt, and next owner");
        }
        int hashAlgorithm = RDataUtils.readU8(rdata, 0);
        int flags = RDataUtils.readU8(rdata, 1);
        int iterations = RDataUtils.readU16(rdata, 2);
        int saltLength = RDataUtils.readU8(rdata, 4);
        int idx = 5;
        if (idx + saltLength + 1 > rdata.length) {
            throw new IllegalArgumentException("NSEC3 salt length exceeds RDATA length");
        }
        byte[] salt = new byte[saltLength];
        if (saltLength > 0) {
            System.arraycopy(rdata, idx, salt, 0, saltLength);
        }
        idx += saltLength;
        int nextLength = RDataUtils.readU8(rdata, idx);
        idx += 1;
        if (idx + nextLength > rdata.length) {
            throw new IllegalArgumentException("NSEC3 next owner length exceeds RDATA length");
        }
        byte[] nextHashedOwner = new byte[nextLength];
        if (nextLength > 0) {
            System.arraycopy(rdata, idx, nextHashedOwner, 0, nextLength);
        }
        idx += nextLength;
        if (idx >= rdata.length) {
            throw new IllegalArgumentException("NSEC3 type bitmaps are missing");
        }
        byte[] typeBitmaps = new byte[rdata.length - idx];
        System.arraycopy(rdata, idx, typeBitmaps, 0, typeBitmaps.length);
        return Nsec3RecordDataDom.builder()
                .hashAlgorithm(hashAlgorithm)
                .flags(flags)
                .iterations(iterations)
                .salt(salt)
                .nextHashedOwner(nextHashedOwner)
                .typeBitmaps(typeBitmaps)
                .build();
    }

    @Override
    public byte[] to() {
        if (hashAlgorithm < 0 || hashAlgorithm > 0xFF) {
            throw new IllegalArgumentException("NSEC3 hash algorithm must be between 0 and 255");
        }
        if (flags < 0 || flags > 0xFF) {
            throw new IllegalArgumentException("NSEC3 flags must be between 0 and 255");
        }
        if (iterations < 0 || iterations > 0xFFFF) {
            throw new IllegalArgumentException("NSEC3 iterations must be between 0 and 65535");
        }
        if (salt == null) {
            throw new IllegalArgumentException("NSEC3 salt is null");
        }
        if (salt.length > 0xFF) {
            throw new IllegalArgumentException("NSEC3 salt length must be between 0 and 255");
        }
        if (nextHashedOwner == null) {
            throw new IllegalArgumentException("NSEC3 next hashed owner is null");
        }
        if (nextHashedOwner.length > 0xFF) {
            throw new IllegalArgumentException("NSEC3 next hashed owner length must be between 0 and 255");
        }
        if (typeBitmaps == null || typeBitmaps.length == 0) {
            throw new IllegalArgumentException("NSEC3 type bitmaps must not be empty");
        }
        byte[] out = new byte[6 + salt.length + nextHashedOwner.length + typeBitmaps.length];
        int idx = 0;
        out[idx++] = (byte) (hashAlgorithm & 0xFF);
        out[idx++] = (byte) (flags & 0xFF);
        out[idx++] = (byte) ((iterations >> 8) & 0xFF);
        out[idx++] = (byte) (iterations & 0xFF);
        out[idx++] = (byte) (salt.length & 0xFF);
        if (salt.length > 0) {
            System.arraycopy(salt, 0, out, idx, salt.length);
            idx += salt.length;
        }
        out[idx++] = (byte) (nextHashedOwner.length & 0xFF);
        if (nextHashedOwner.length > 0) {
            System.arraycopy(nextHashedOwner, 0, out, idx, nextHashedOwner.length);
            idx += nextHashedOwner.length;
        }
        System.arraycopy(typeBitmaps, 0, out, idx, typeBitmaps.length);
        return out;
    }
}
