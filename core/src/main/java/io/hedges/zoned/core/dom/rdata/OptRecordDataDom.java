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

    public static RDataDom from(byte[] rdata) {
        throw new RuntimeException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new RuntimeException("Not Implemented"); //TODO
    }
}
