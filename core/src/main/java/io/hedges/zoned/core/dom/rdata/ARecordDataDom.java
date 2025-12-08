package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@Getter
@Builder
@ToString
public class ARecordDataDom implements RDataDom {
    private Inet4Address address;
    @Override
    public RDataDom from(byte[] rdata) {
        this.address = RDataUtils.toInet4Address(rdata);
        return this;
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(address);
    }
}
