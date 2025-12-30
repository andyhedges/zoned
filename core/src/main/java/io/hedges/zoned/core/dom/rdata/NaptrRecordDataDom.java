// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS NAPTR record RDATA.
 *
 * <p>RDATA is order (16-bit), preference (16-bit), flags (character-string),
 * services (character-string), regexp (character-string), and replacement name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Order     | 2            | NAPTR order.               |
 * | Preference| 2            | NAPTR preference.          |
 * | Flags     | variable     | Flags character-string.    |
 * | Services  | variable     | Services character-string. |
 * | Regexp    | variable     | Regexp character-string.   |
 * | Replacement| variable    | Replacement name.          |
 * +-----------+--------------+----------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class NaptrRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
