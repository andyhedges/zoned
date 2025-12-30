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
*/
@Getter
@Builder
@ToString
public class Nsec3RecordDataDom implements RDataDom {
    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
