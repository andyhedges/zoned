// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.NameResolver;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class MxRecordDataDom implements RDataDom {
    private int preference;
    private DnsNameDom exchange;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        if (rdata == null || rdata.length <= 2) {
            throw new IllegalArgumentException("MX RDATA requires a 2-byte preference and exchange name");
        }
        int preference = RDataUtils.readU16(rdata, 0);
        byte[] nameBytes = new byte[rdata.length - 2];
        System.arraycopy(rdata, 2, nameBytes, 0, nameBytes.length);
        DnsNameDom exchange = RDataUtils.toDnsNameDom(nameBytes, resolver);
        return MxRecordDataDom.builder()
                .preference(preference)
                .exchange(exchange)
                .build();
    }

    @Override
    public byte[] to() {
        if (exchange == null) {
            throw new IllegalArgumentException("MX RDATA requires an exchange name");
        }
        if (preference < 0 || preference > 0xFFFF) {
            throw new IllegalArgumentException("MX preference must be between 0 and 65535");
        }
        byte[] nameBytes = RDataUtils.toByteArray(exchange);
        byte[] out = new byte[nameBytes.length + 2];
        out[0] = (byte) ((preference >> 8) & 0xFF);
        out[1] = (byte) (preference & 0xFF);
        System.arraycopy(nameBytes, 0, out, 2, nameBytes.length);
        return out;
    }
}
