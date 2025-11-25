package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.rdata.edns.EdnsOptionDom;

import java.util.List;

public class OptRecordDataDom implements RDataDom {
    private int udpPayloadSize;
    private int extendedRCode;
    private int version;
    private boolean dnssecOk;
    private List<EdnsOptionDom> ednsOptions;
}
