// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS CNAME record RDATA.
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

 * <p>Purpose: Creates an alias from one domain name to another so queries are redirected to the canonical name.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc1035">RFC 1035</a>.</p>*/
@Getter
@Builder
@ToString
public class CnameRecordDataDom implements RDataDom {
    private DnsNameDom cname;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        return CnameRecordDataDom.builder()
                .cname(RDataUtils.toDnsNameDom(rdata, resolver))
                .build();
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(cname);
    }

}
