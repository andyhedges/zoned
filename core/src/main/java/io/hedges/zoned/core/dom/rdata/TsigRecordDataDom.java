package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class TsigRecordDataDom implements RDataDom {

    @Override
    public RDataDom from(byte[] raw) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public byte[] to() {
        throw new RuntimeException("Not Implemented");
    }
}
