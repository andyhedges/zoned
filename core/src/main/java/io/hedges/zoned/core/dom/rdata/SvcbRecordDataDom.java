// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.SortedMap;

/**
 * Domain model for DNS SVCB record RDATA.
 *
 * <p>RDATA is SvcPriority (16-bit), target name, and service parameters.</p>
 
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

 * <p>Purpose: Publishes Service Binding (SVCB) data describing alternative endpoints and parameters.</p>
 * <p>It enables clients to discover service configuration (such as protocols, ports, or keys) via DNS,
 * often improving connection setup and performance.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc9460">RFC 9460</a>.</p>*/
@Getter
@Builder
@ToString
public class SvcbRecordDataDom extends SvcbLikeRecordDataDom {

    private int svcPriority;
    private DnsNameDom targetName;
    private SortedMap<Integer, byte[]> svcParams;

    public static RDataDom from(byte[] rdata) {
        return from(rdata, null);
    }

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        SvcbRdataCodec.SvcbRdataFields parsed = SvcbRdataCodec.parse(rdata, resolver, "SVCB");
        return SvcbRecordDataDom.builder()
                .svcPriority(parsed.svcPriority())
                .targetName(parsed.targetName())
                .svcParams(parsed.svcParams())
                .build();
    }

    @Override
    protected String typeLabel() {
        return "SVCB";
    }
}
