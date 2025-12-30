// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS SRV record RDATA.
 *
 * <p>RDATA is priority (16-bit), weight (16-bit), port (16-bit), and target name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Priority  | 2            | Service priority.          |
 * | Weight    | 2            | Load balancing weight.     |
 * | Port      | 2            | Service port.              |
 * | Target    | variable     | Target name.               |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Locates services by name, providing priority, weight, port, and target host.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc2782">RFC 2782</a>.</p>*/
@Getter
@Builder
@ToString
public class SrvRecordDataDom implements RDataDom {
    private int priority;
    private int weight;
    private int port;
    private DnsNameDom target;

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
