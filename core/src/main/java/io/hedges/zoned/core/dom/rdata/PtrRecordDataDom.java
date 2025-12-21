package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PtrRecordDataDom implements RDataDom {

    private DnsNameDom ptrName;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        return PtrRecordDataDom.builder()
                .ptrName(RDataUtils.toDnsNameDom(rdata, resolver))
                .build();
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(ptrName);
    }
}
