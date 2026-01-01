// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.NameResolver;
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
        return from(rdata, null);
    }

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        if (rdata == null || rdata.length <= 6) {
            throw new IllegalArgumentException("SRV RDATA requires priority, weight, port, and target name");
        }
        int priority = RDataUtils.readU16(rdata, 0);
        int weight = RDataUtils.readU16(rdata, 2);
        int port = RDataUtils.readU16(rdata, 4);
        RDataUtils.DnsNameParseResult parsed = RDataUtils.parseDnsName(rdata, 6, resolver);
        if (parsed.nextIndex() != rdata.length) {
            throw new IllegalArgumentException("Extra bytes after SRV target");
        }
        return SrvRecordDataDom.builder()
                .priority(priority)
                .weight(weight)
                .port(port)
                .target(parsed.name())
                .build();
    }

    @Override
    public byte[] to() {
        if (target == null) {
            throw new IllegalArgumentException("SRV RDATA requires a target name");
        }
        if (priority < 0 || priority > 0xFFFF) {
            throw new IllegalArgumentException("SRV priority must be between 0 and 65535");
        }
        if (weight < 0 || weight > 0xFFFF) {
            throw new IllegalArgumentException("SRV weight must be between 0 and 65535");
        }
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("SRV port must be between 0 and 65535");
        }
        byte[] nameBytes = RDataUtils.toByteArray(target);
        byte[] out = new byte[6 + nameBytes.length];
        out[0] = (byte) ((priority >> 8) & 0xFF);
        out[1] = (byte) (priority & 0xFF);
        out[2] = (byte) ((weight >> 8) & 0xFF);
        out[3] = (byte) (weight & 0xFF);
        out[4] = (byte) ((port >> 8) & 0xFF);
        out[5] = (byte) (port & 0xFF);
        System.arraycopy(nameBytes, 0, out, 6, nameBytes.length);
        return out;
    }
}
