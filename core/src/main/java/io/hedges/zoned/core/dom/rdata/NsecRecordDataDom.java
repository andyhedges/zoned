// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS NSEC record RDATA.
 *
 * <p>RDATA is next domain name followed by type bitmaps.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | NextName  | variable     | Next domain name.          |
 * | TypeMap   | variable     | Type bitmaps.              |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Provides DNSSEC authenticated denial of existence by indicating the next name and present types.</p>
 * <p>NSEC records form a chain through the zone and can be used to prove that a name or type does not exist,
 * but they also enable zone enumeration.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc4034">RFC 4034</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class NsecRecordDataDom implements RDataDom {
    private DnsNameDom nextName;
    private byte[] typeBitmaps;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        if (rdata == null || rdata.length == 0) {
            throw new IllegalArgumentException("NSEC RDATA is empty");
        }
        RDataUtils.DnsNameParseResult parsed = RDataUtils.parseDnsName(rdata, 0, resolver);
        int idx = parsed.nextIndex();
        if (idx >= rdata.length) {
            throw new IllegalArgumentException("NSEC type bitmaps are missing");
        }
        byte[] typeBitmaps = new byte[rdata.length - idx];
        System.arraycopy(rdata, idx, typeBitmaps, 0, typeBitmaps.length);
        return NsecRecordDataDom.builder()
                .nextName(parsed.name())
                .typeBitmaps(typeBitmaps)
                .build();
    }

    @Override
    public byte[] to() {
        if (nextName == null) {
            throw new IllegalArgumentException("NSEC next name is null");
        }
        byte[] nameBytes = RDataUtils.toByteArray(nextName);
        if (typeBitmaps == null || typeBitmaps.length == 0) {
            throw new IllegalArgumentException("NSEC type bitmaps must not be empty");
        }
        byte[] out = new byte[nameBytes.length + typeBitmaps.length];
        System.arraycopy(nameBytes, 0, out, 0, nameBytes.length);
        System.arraycopy(typeBitmaps, 0, out, nameBytes.length, typeBitmaps.length);
        return out;
    }
}
