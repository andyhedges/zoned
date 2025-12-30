// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS NSEC record RDATA.
 *
 * <p>RDATA is next domain name followed by type bitmaps.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | NextName  | variable     | Next domain name.          |
 * | TypeMap   | variable     | Type bitmaps.              |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Provides DNSSEC authenticated denial of existence by indicating the next name and present types.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc4034">RFC 4034</a>.</p>*/
@Getter
@Builder
@ToString
public class NsecRecordDataDom implements RDataDom {
    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
