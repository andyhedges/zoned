package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@Getter
@Builder
@ToString
public class AAAARecordDataDom implements RDataDom {

    private Inet6Address address;

    @Override
    public RDataDom from(byte[] rdata) {
        this.address = RDataUtils.toInet6Address(rdata);
        return this;
    }

    @Override
    public byte[] to() {
        return RDataUtils.toByteArray(address);
    }
}
