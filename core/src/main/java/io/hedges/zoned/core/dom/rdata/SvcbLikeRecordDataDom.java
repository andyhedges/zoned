// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;

import java.util.SortedMap;

abstract class SvcbLikeRecordDataDom implements RDataDom {

    @Override
    public byte[] to() {
        return SvcbRdataCodec.encode(svcPriority(), targetName(), svcParams(), typeLabel());
    }

    protected abstract int svcPriority();

    protected abstract DnsNameDom targetName();

    protected abstract SortedMap<Integer, byte[]> svcParams();

    protected abstract String typeLabel();
}
