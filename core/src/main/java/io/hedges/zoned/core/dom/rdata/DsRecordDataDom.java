package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class DsRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new RuntimeException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new RuntimeException("Not Implemented"); //TODO
    }
}
