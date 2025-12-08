package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class NsRecordDataDom implements RDataDom {
    private DnsNameDom nsdname;

    @Override
    public void from(byte[] raw) {
        this.nsdname = RDataUtils.toDnsNameDom(raw);
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(this.nsdname);
    }
}
