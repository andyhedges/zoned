package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CnameRecordDataDom implements RDataDom {
    private DnsNameDom cname;

    public static RDataDom from(byte[] rdata) {
        return CnameRecordDataDom.builder()
                .cname(RDataUtils.toDnsNameDom(rdata))
                .build();
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(cname);
    }

}
