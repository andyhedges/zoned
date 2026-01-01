// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.net.Inet6Address;

/**
 * Domain model for DNS AAAA record RDATA.
 *
 * <p>RDATA is a 16-octet IPv6 address.</p>
 
 *
 * <pre>
 * +----------+--------------+------------------------+
 * | Field    | Size (octets)| Description            |
 * +----------+--------------+------------------------+
 * | Address  | 16           | IPv6 address.          |
 * +----------+--------------+------------------------+
 * </pre>

 * <p>Purpose: Maps a host name to an IPv6 address so clients can reach a specific interface.</p>
 * <p>It serves the same role as A records for modern networks and is commonly published alongside
 * A records for dual-stack connectivity and failover.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc3596">RFC 3596</a>.</p>*/
@Getter
@Builder
@ToString
public class AAAARecordDataDom implements RDataDom {

    private Inet6Address address;

    public static RDataDom from(byte[] rdata) {
        return AAAARecordDataDom.builder()
                .address(RDataUtils.toInet6Address(rdata))
                .build();
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(address);
    }
}
