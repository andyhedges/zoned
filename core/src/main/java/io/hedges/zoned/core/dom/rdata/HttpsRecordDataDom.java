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
 */
@Getter
@Builder
@ToString
public class HttpsRecordDataDom implements RDataDom {

    private int svcPriority;
    private DnsNameDom targetName;
    private SortedMap<Integer, byte[]> svcParams;

    public static RDataDom from(byte[] rdata) {
        return from(rdata, null);
    }

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        SvcbRdataCodec.SvcbRdataFields parsed = SvcbRdataCodec.parse(rdata, resolver, "HTTPS");
        return HttpsRecordDataDom.builder()
                .svcPriority(parsed.svcPriority())
                .targetName(parsed.targetName())
                .svcParams(parsed.svcParams())
                .build();
    }

    @Override
    public byte[] to() {
        return SvcbRdataCodec.encode(svcPriority, targetName, svcParams, "HTTPS");
    }
}
