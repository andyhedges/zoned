// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS DNAME record RDATA.
 *
 * <p>RDATA is a target domain name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Target    | variable     | Domain name (wire format). |
 * +-----------+--------------+----------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class DnameRecordDataDom implements RDataDom {
    private DnsNameDom dname;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        return DnameRecordDataDom.builder()
                .dname(RDataUtils.toDnsNameDom(rdata, resolver))
                .build();
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(dname);
    }
}
