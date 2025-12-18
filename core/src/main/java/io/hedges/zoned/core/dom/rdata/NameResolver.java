package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;

@FunctionalInterface
public interface NameResolver {

    DnsNameDom resolve(int absoluteOffset);
}
