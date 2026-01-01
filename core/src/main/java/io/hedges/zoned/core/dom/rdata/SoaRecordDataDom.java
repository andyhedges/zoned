// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS SOA record RDATA.
 *
 * <p>RDATA is mname, rname, serial, refresh, retry, expire, and minimum TTL.</p>
 
 *
 * <pre>
 * +-------------+--------------+----------------------------+
 * | Field       | Size (octets)| Description                |
 * +-------------+--------------+----------------------------+
 * | MNAME       | variable     | Primary name server.       |
 * | RNAME       | variable     | Responsible mailbox.       |
 * | Serial      | 4            | Zone serial number.        |
 * | Refresh     | 4            | Refresh interval.          |
 * | Retry       | 4            | Retry interval.            |
 * | Expire      | 4            | Expire interval.           |
 * | Minimum     | 4            | Minimum TTL.               |
 * +-------------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Defines the Start of Authority (SOA) for a zone, including serial and timing parameters.</p>
 * <p>It drives zone transfers and negative caching behavior by publishing the authoritative serial number
 * and refresh/retry/expire/minimum timers used by secondaries and resolvers.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc1035">RFC 1035</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class SoaRecordDataDom implements RDataDom {
    private DnsNameDom mname;
    private DnsNameDom rname;
    private long serial;
    private long refreshSeconds;
    private long retrySeconds;
    private long expireSeconds;
    private long minimumTtlSeconds;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        if (rdata == null) {
            throw new IllegalArgumentException("SOA RDATA cannot be null");
        }
        if (rdata.length < 22) {
            throw new IllegalArgumentException("SOA RDATA is too short");
        }
        RDataUtils.DnsNameParseResult mnameResult = RDataUtils.parseDnsName(rdata, 0, resolver);
        int idx = mnameResult.nextIndex();
        if (idx >= rdata.length) {
            throw new IllegalArgumentException("SOA RDATA missing rname");
        }
        RDataUtils.DnsNameParseResult rnameResult = RDataUtils.parseDnsName(rdata, idx, resolver);
        idx = rnameResult.nextIndex();

        int remaining = rdata.length - idx;
        if (remaining != 20) {
            throw new IllegalArgumentException("SOA RDATA must have 20 bytes after names");
        }

        long serial = RDataUtils.readU32(rdata, idx);
        idx += 4;
        long refresh = RDataUtils.readU32(rdata, idx);
        idx += 4;
        long retry = RDataUtils.readU32(rdata, idx);
        idx += 4;
        long expire = RDataUtils.readU32(rdata, idx);
        idx += 4;
        long minimum = RDataUtils.readU32(rdata, idx);

        return SoaRecordDataDom.builder()
                .mname(mnameResult.name())
                .rname(rnameResult.name())
                .serial(serial)
                .refreshSeconds(refresh)
                .retrySeconds(retry)
                .expireSeconds(expire)
                .minimumTtlSeconds(minimum)
                .build();
    }

    @Override
    public byte[] to() {
        if (mname == null) {
            throw new IllegalArgumentException("SOA RDATA requires mname");
        }
        if (rname == null) {
            throw new IllegalArgumentException("SOA RDATA requires rname");
        }
        validateU32(serial, "serial");
        validateU32(refreshSeconds, "refresh");
        validateU32(retrySeconds, "retry");
        validateU32(expireSeconds, "expire");
        validateU32(minimumTtlSeconds, "minimum");

        byte[] mnameBytes = RDataUtils.toByteArray(mname);
        byte[] rnameBytes = RDataUtils.toByteArray(rname);
        byte[] out = new byte[mnameBytes.length + rnameBytes.length + 20];
        int idx = 0;
        System.arraycopy(mnameBytes, 0, out, idx, mnameBytes.length);
        idx += mnameBytes.length;
        System.arraycopy(rnameBytes, 0, out, idx, rnameBytes.length);
        idx += rnameBytes.length;
        RDataUtils.writeU32(out, idx, serial);
        idx += 4;
        RDataUtils.writeU32(out, idx, refreshSeconds);
        idx += 4;
        RDataUtils.writeU32(out, idx, retrySeconds);
        idx += 4;
        RDataUtils.writeU32(out, idx, expireSeconds);
        idx += 4;
        RDataUtils.writeU32(out, idx, minimumTtlSeconds);
        return out;
    }

    private static void validateU32(long value, String field) {
        if (value < 0 || value > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("SOA " + field + " must be between 0 and 4294967295");
        }
    }
}
