// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;

/**
 * Domain model for DNS URI record RDATA.
 *
 * <p>RDATA is priority (16-bit), weight (16-bit), and target URI bytes.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Priority  | 2            | URI priority.              |
 * | Weight    | 2            | URI weight.                |
 * | Target    | variable     | URI bytes.                 |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes Uniform Resource Identifier (URI) data for a domain name.</p>
 * <p>Priority and weight enable selection among multiple URIs, similar to SRV, and allow clients
 * to discover application-specific endpoints via DNS.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc7553">RFC 7553</a>.</p>*/
@Getter
@Builder
@ToString
public class UriRecordDataDom implements RDataDom {

    private int priority;
    private int weight;
    private String target;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length < 4) {
            throw new IllegalArgumentException("URI RDATA requires priority, weight, and target");
        }
        int priority = RDataUtils.readU16(rdata, 0);
        int weight = RDataUtils.readU16(rdata, 2);
        byte[] targetBytes = new byte[rdata.length - 4];
        if (targetBytes.length > 0) {
            System.arraycopy(rdata, 4, targetBytes, 0, targetBytes.length);
        }
        String target = new String(targetBytes, StandardCharsets.UTF_8);
        return UriRecordDataDom.builder()
                .priority(priority)
                .weight(weight)
                .target(target)
                .build();
    }

    @Override
    public byte[] to() {
        if (priority < 0 || priority > 0xFFFF) {
            throw new IllegalArgumentException("URI priority must be between 0 and 65535");
        }
        if (weight < 0 || weight > 0xFFFF) {
            throw new IllegalArgumentException("URI weight must be between 0 and 65535");
        }
        if (target == null) {
            throw new IllegalArgumentException("URI target must not be null");
        }
        byte[] targetBytes = target.getBytes(StandardCharsets.UTF_8);
        if (targetBytes.length > 0xFF) {
            throw new IllegalArgumentException("URI target exceeds 255 bytes");
        }
        byte[] out = new byte[4 + targetBytes.length];
        out[0] = (byte) ((priority >> 8) & 0xFF);
        out[1] = (byte) (priority & 0xFF);
        out[2] = (byte) ((weight >> 8) & 0xFF);
        out[3] = (byte) (weight & 0xFF);
        if (targetBytes.length > 0) {
            System.arraycopy(targetBytes, 0, out, 4, targetBytes.length);
        }
        return out;
    }
}
