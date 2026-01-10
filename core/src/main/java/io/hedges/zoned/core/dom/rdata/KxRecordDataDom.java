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
 * Domain model for DNS KX record RDATA.
 *
 * <p>RDATA is preference (16-bit) and exchanger domain name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Preference| 2            | Key exchange preference.   |
 * | Exchanger | variable     | Domain name (wire format). |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Specifies a Key Exchange (KX) server for a domain, similar to MX but for key exchange services.</p>
 * <p>The preference value orders candidate servers, allowing fallback and load distribution for
 * key exchange endpoints.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc2230">RFC 2230</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class KxRecordDataDom implements RDataDom {
    private int preference;
    private DnsNameDom exchanger;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        if (rdata == null || rdata.length <= 2) {
            throw new IllegalArgumentException("KX RDATA requires a 2-byte preference and exchanger name");
        }
        int preference = RDataUtils.readU16(rdata, 0);
        byte[] nameBytes = new byte[rdata.length - 2];
        System.arraycopy(rdata, 2, nameBytes, 0, nameBytes.length);
        DnsNameDom exchanger = RDataUtils.toDnsNameDom(nameBytes, resolver, DnsNameDomPolicy.Builtin.HOSTNAME);
        return KxRecordDataDom.builder()
                .preference(preference)
                .exchanger(exchanger)
                .build();
    }

    @Override
    public byte[] to() {
        if (exchanger == null) {
            throw new IllegalArgumentException("KX RDATA requires an exchanger name");
        }
        if (preference < 0 || preference > 0xFFFF) {
            throw new IllegalArgumentException("KX preference must be between 0 and 65535");
        }
        byte[] nameBytes = RDataUtils.toByteArray(exchanger);
        byte[] out = new byte[nameBytes.length + 2];
        out[0] = (byte) ((preference >> 8) & 0xFF);
        out[1] = (byte) (preference & 0xFF);
        System.arraycopy(nameBytes, 0, out, 2, nameBytes.length);
        return out;
    }
}
