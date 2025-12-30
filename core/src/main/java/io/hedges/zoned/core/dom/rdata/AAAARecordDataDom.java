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
*/
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
