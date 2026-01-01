// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS NS record RDATA.
 *
 * <p>RDATA is a name server domain name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | NSDNAME   | variable     | Domain name (wire format). |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Lists authoritative Name Server (NS) hosts for a zone.</p>
 * <p>These records appear at a zone's apex and at delegation points, guiding resolvers to the servers
 * that can answer for that zone's data.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc1035">RFC 1035</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class NsRecordDataDom implements RDataDom {
    private DnsNameDom nsName;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        return NsRecordDataDom.builder()
                .nsName(RDataUtils.toDnsNameDom(rdata, resolver))
                .build();
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(this.nsName);
    }
}
