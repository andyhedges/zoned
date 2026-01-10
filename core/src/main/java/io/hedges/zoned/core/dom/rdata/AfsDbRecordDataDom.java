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
 * Domain model for DNS AFSDB record RDATA.
 *
 * <p>RDATA is a 16-bit subtype followed by a host name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Subtype   | 2            | AFSDB subtype.             |
 * | Hostname  | variable     | Domain name (wire format). |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes Andrew File System (AFS) database server information so clients can locate AFS services.</p>
 * <p>The subtype indicates the specific AFS-related service, and the hostname points clients to the
 * appropriate server for that service.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc1183">RFC 1183</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class AfsDbRecordDataDom implements RDataDom {
    private int subtype;
    private DnsNameDom hostname;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        if (rdata == null) {
            throw new IllegalArgumentException("AFSDB RDATA cannot be null");
        }
        if (rdata.length < 4) {
            throw new IllegalArgumentException("AFSDB RDATA must be at least 4 bytes");
        }
        int subtype = RDataUtils.readU16(rdata, 0);
        RDataUtils.DnsNameParseResult nameResult = RDataUtils.parseDnsName(
                rdata,
                2,
                resolver,
                DnsNameDomPolicy.Builtin.HOSTNAME);
        if (nameResult.nextIndex() != rdata.length) {
            throw new IllegalArgumentException("Extra bytes after AFSDB hostname");
        }
        DnsNameDom hostname = nameResult.name();
        return AfsDbRecordDataDom.builder()
                .subtype(subtype)
                .hostname(hostname)
                .build();
    }

    @Override
    public byte[] to() {
        if (hostname == null) {
            throw new IllegalArgumentException("AFSDB RDATA requires a hostname");
        }
        if (subtype < 0 || subtype > 0xFFFF) {
            throw new IllegalArgumentException("AFSDB subtype must be between 0 and 65535");
        }
        byte[] nameBytes = RDataUtils.toByteArray(hostname);
        byte[] out = new byte[nameBytes.length + 2];
        out[0] = (byte) ((subtype >> 8) & 0xFF);
        out[1] = (byte) (subtype & 0xFF);
        System.arraycopy(nameBytes, 0, out, 2, nameBytes.length);
        return out;
    }
}
