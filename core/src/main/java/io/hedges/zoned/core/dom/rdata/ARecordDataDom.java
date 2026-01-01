// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.net.Inet4Address;

/**
 * Domain model for DNS A record RDATA.
 *
 * <p>RDATA is a 4-octet IPv4 address.</p>
 
 *
 * <pre>
 * +----------+--------------+------------------------+
 * | Field    | Size (octets)| Description            |
 * +----------+--------------+------------------------+
 * | Address  | 4            | IPv4 address.          |
 * +----------+--------------+------------------------+
 * </pre>

 * <p>Purpose: Maps a host name to an IPv4 address so clients can reach a specific interface.</p>
 * <p>This is the most common forward-lookup record and is frequently published in sets to provide
 * redundancy, load distribution, or multi-homing.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc1035">RFC 1035</a>.</p>*/
@Getter
@Builder
@ToString
public class ARecordDataDom implements RDataDom {
    private Inet4Address address;

    public static RDataDom from(byte[] rdata) {
        return ARecordDataDom.builder()
                .address(RDataUtils.toInet4Address(rdata))
                .build();
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(address);
    }
}
