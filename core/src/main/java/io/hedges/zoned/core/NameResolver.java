package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsNameDom;

@FunctionalInterface
public interface NameResolver {

    DnsNameDom resolve(int absoluteOffset);
}
