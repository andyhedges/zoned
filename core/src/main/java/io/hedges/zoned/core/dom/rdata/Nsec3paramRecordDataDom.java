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
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc5155">RFC 5155</a>.</p>*/
@Getter
@Builder
@ToString
public class Nsec3paramRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
