package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.rdata.edns.EdnsOptionDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
@Getter
@Builder
@ToString
public class OptRecordDataDom implements RDataDom {
    private int udpPayloadSize;
    private int extendedRCode;
    private int version;
    private boolean dnssecOk;
    private List<EdnsOptionDom> ednsOptions;

    @Override
    public RDataDom from(byte[] raw) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public byte[] to() {
        throw new RuntimeException("Not Implemented");
    }
}
