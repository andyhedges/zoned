// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS ZONEMD record RDATA.
 *
 * <p>RDATA is serial (32-bit), scheme (8-bit), hash algorithm (8-bit), and digest.</p>
 *
 * <pre>
 * +---------------+--------------+----------------------------+
 * | Field         | Size (octets)| Description                |
 * +---------------+--------------+----------------------------+
 * | Serial        | 4            | SOA serial at calculation. |
 * | Scheme        | 1            | Digest scheme.             |
 * | HashAlgorithm | 1            | Digest algorithm.          |
 * | Digest        | variable     | Zone digest bytes.         |
 * +---------------+--------------+----------------------------+
 * </pre>
 
 * <p>Purpose: Publishes zone content digests so consumers can verify zone integrity.</p>
 * <p>It allows offline or secondary consumers to validate that a zone's contents match the published
 * digest for a given serial and hashing scheme.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc8976">RFC 8976</a>.</p>*/
@Getter
@Builder
@ToString
public class ZonemdRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
