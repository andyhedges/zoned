// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsNameDomPolicy;
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

 * <p>Purpose: Redirects an entire subtree of the DNS namespace to another domain, effectively aliasing all names under a node.</p>
 * <p>Unlike CNAME, DNAME applies to all names beneath the owner name while allowing the owner itself to
 * have other record types.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc6672">RFC 6672</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class DnameRecordDataDom implements RDataDom {
    private DnsNameDom dname;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        return DnameRecordDataDom.builder()
                .dname(RDataUtils.toDnsNameDom(rdata, resolver, DnsNameDomPolicy.Builtin.PROTOCOL))
                .build();
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(dname);
    }
}
