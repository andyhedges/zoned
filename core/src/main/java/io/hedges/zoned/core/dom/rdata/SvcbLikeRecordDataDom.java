// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;

import java.util.SortedMap;

/**
 * Shared base for SVCB-like RDATA encodings.
 *
 * <p>RDATA is SvcPriority (16-bit), target name, and a set of service parameters.</p>
 
 *
 * <pre>
 * +-------------+--------------+----------------------------+
 * | Field       | Size (octets)| Description                |
 * +-------------+--------------+----------------------------+
 * | SvcPriority | 2            | Service priority.          |
 * | TargetName  | variable     | Target name.               |
 * | SvcParams   | variable     | Service parameters.        |
 * +-------------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Base representation for Service Binding (SVCB) and HTTPS records.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc9460">RFC 9460</a>.</p>*/
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
