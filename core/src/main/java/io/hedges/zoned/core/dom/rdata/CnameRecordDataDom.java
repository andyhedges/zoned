package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@ToString
public class CnameRecordDataDom implements RDataDom {
    private DnsNameDom cname;

    @Override
    public RDataDom from(byte[] rdata) {
        cname = RDataUtils.toDnsNameDom(rdata);
        return this;
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(cname);
    }

}
