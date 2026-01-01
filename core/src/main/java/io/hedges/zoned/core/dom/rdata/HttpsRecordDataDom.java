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
 * Domain model for HTTPS (SVCB-based) RDATA as defined in RFC 9460.
 *
 * <p>The RDATA is encoded as a 16-bit SvcPriority followed by a target name
 * and zero or more service parameters. The {@code svcParams} map stores
 * parameter keys with their raw value bytes.</p>
 *
 * <pre>
 * +---------------+--------------+--------------------------------------------------------+
 * | Field         | Size (octets)| Description                                            |
 * +---------------+--------------+--------------------------------------------------------+
 * | SvcPriority   | 2            | Service priority for the SVCB/HTTPS record.           |
 * | TargetName    | variable     | DNS name, wire-encoded labels, terminated by zero.    |
 * | SvcParamKey   | 2            | Service parameter key (repeat for each parameter).    |
 * | SvcParamLen   | 2            | Length of the parameter value in octets.              |
 * | SvcParamValue | variable     | Opaque parameter value bytes.                         |
 * +---------------+--------------+--------------------------------------------------------+
 * </pre>
 
 * <p>Purpose: Publishes HTTPS Service Binding (SVCB) data that informs clients how to reach HTTPS services.</p>
 * <p>It can advertise alternative endpoints, protocols, and configuration hints for HTTPS connections,
 * enabling clients to optimize connection establishment.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc9460">RFC 9460</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class HttpsRecordDataDom extends SvcbLikeRecordDataDom {

    private int svcPriority;
    private DnsNameDom targetName;
    private SortedMap<Integer, byte[]> svcParams;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        SvcbRdataCodec.SvcbRdataFields parsed = SvcbRdataCodec.parse(rdata, resolver, "HTTPS");
        return HttpsRecordDataDom.builder()
                .svcPriority(parsed.svcPriority())
                .targetName(parsed.targetName())
                .svcParams(parsed.svcParams())
                .build();
    }

    @Override
    protected String typeLabel() {
        return "HTTPS";
    }
}
