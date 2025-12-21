package io.hedges.zoned.core.dom.rdata;

import java.util.SortedMap;
import java.util.TreeMap;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
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
        if (rdata == null || rdata.length < 3) {
            throw new IllegalArgumentException("HTTPS RDATA requires priority and target name");
        }
        int svcPriority = RDataUtils.readU16(rdata, 0);
        RDataUtils.DnsNameParseResult parsedName = RDataUtils.parseDnsName(rdata, 2, resolver);
        DnsNameDom targetName = parsedName.name();
        int idx = parsedName.nextIndex();

        SortedMap<Integer, byte[]> params = new TreeMap<>();
        while (idx < rdata.length) {
            if (idx + 4 > rdata.length) {
                throw new IllegalArgumentException("HTTPS service parameter header is truncated");
            }
            int key = RDataUtils.readU16(rdata, idx);
            int len = RDataUtils.readU16(rdata, idx + 2);
            idx += 4;
            if (idx + len > rdata.length) {
                throw new IllegalArgumentException("HTTPS service parameter exceeds RDATA bounds");
            }
            byte[] value = new byte[len];
            if (len > 0) {
                System.arraycopy(rdata, idx, value, 0, len);
            }
            if (params.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Duplicate HTTPS service parameter key: " + key);
            }
            idx += len;
        }

        return HttpsRecordDataDom.builder()
                .svcPriority(svcPriority)
                .targetName(targetName)
                .svcParams(params)
                .build();
    }

    @Override
    public byte[] to() {
        if (targetName == null) {
            throw new IllegalArgumentException("HTTPS RDATA requires a target name");
        }
        if (svcPriority < 0 || svcPriority > 0xFFFF) {
            throw new IllegalArgumentException("HTTPS SvcPriority must be between 0 and 65535");
        }
        byte[] nameBytes = RDataUtils.toByteArray(targetName);
        int totalLength = 2 + nameBytes.length;
        if (svcParams != null) {
            for (Map.Entry<Integer, byte[]> entry : svcParams.entrySet()) {
                if (entry.getKey() == null) {
                    throw new IllegalArgumentException("HTTPS service parameter key is null");
                }
                int key = entry.getKey();
                if (key < 0 || key > 0xFFFF) {
                    throw new IllegalArgumentException("HTTPS service parameter key out of range: " + key);
                }
                byte[] value = entry.getValue();
                if (value == null) {
                    throw new IllegalArgumentException("HTTPS service parameter value is null for key: " + key);
                }
                if (value.length > 0xFFFF) {
                    throw new IllegalArgumentException("HTTPS service parameter value exceeds 65535 bytes");
                }
                if (totalLength + 4 + value.length > 0xFFFF) {
                    throw new IllegalArgumentException("HTTPS RDATA exceeds 65535 bytes");
                }
                totalLength += 4 + value.length;
            }
        }

        byte[] out = new byte[totalLength];
        int idx = 0;
        out[idx++] = (byte) ((svcPriority >> 8) & 0xFF);
        out[idx++] = (byte) (svcPriority & 0xFF);
        System.arraycopy(nameBytes, 0, out, idx, nameBytes.length);
        idx += nameBytes.length;
        if (svcParams != null) {
            for (Map.Entry<Integer, byte[]> entry : svcParams.entrySet()) {
                int key = entry.getKey();
                byte[] value = entry.getValue();
                int len = value.length;
                out[idx++] = (byte) ((key >> 8) & 0xFF);
                out[idx++] = (byte) (key & 0xFF);
                out[idx++] = (byte) ((len >> 8) & 0xFF);
                out[idx++] = (byte) (len & 0xFF);
                if (len > 0) {
                    System.arraycopy(value, 0, out, idx, len);
                }
                idx += len;
            }
        }
        if (idx != out.length) {
            throw new IllegalStateException("Bug: HTTPS RDATA encoded length mismatch");
        }
        return out;
    }
}
