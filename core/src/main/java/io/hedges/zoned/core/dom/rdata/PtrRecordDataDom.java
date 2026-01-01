// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS PTR record RDATA.
 *
 * <p>RDATA is a domain name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | PTRDNAME  | variable     | Domain name (wire format). |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Maps an IP address to a domain name for reverse lookups.</p>
 * <p>PTR records live under reverse zones (such as in-addr.arpa and ip6.arpa) and are commonly used
 * for diagnostics, logging, and service validation.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc1035">RFC 1035</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class PtrRecordDataDom implements RDataDom {

    private DnsNameDom ptrName;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        return PtrRecordDataDom.builder()
                .ptrName(RDataUtils.toDnsNameDom(rdata, resolver))
                .build();
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(ptrName);
    }
}
