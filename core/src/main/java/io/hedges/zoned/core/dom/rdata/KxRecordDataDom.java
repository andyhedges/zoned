// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS KX record RDATA.
 *
 * <p>RDATA is preference (16-bit) and exchanger domain name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Preference| 2            | Key exchange preference.   |
 * | Exchanger | variable     | Domain name (wire format). |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Specifies a Key Exchange (KX) server for a domain, similar to MX but for key exchange services.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc2230">RFC 2230</a>.</p>*/
@Getter
@Builder
@ToString
public class KxRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
