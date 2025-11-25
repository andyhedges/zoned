package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.net.Inet6Address;

@Getter
@Builder
@ToString
public class AAAARecordDataDom implements RDataDom {
    private Inet6Address address;
}
