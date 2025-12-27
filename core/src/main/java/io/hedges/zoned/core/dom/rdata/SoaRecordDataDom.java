package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
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

        long serial = readU32(rdata, idx);
        idx += 4;
        long refresh = readU32(rdata, idx);
        idx += 4;
        long retry = readU32(rdata, idx);
        idx += 4;
        long expire = readU32(rdata, idx);
        idx += 4;
        long minimum = readU32(rdata, idx);

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
        writeU32(out, idx, serial);
        idx += 4;
        writeU32(out, idx, refreshSeconds);
        idx += 4;
        writeU32(out, idx, retrySeconds);
        idx += 4;
        writeU32(out, idx, expireSeconds);
        idx += 4;
        writeU32(out, idx, minimumTtlSeconds);
        return out;
    }

    private static long readU32(byte[] rdata, int offset) {
        if (rdata == null) {
            throw new IllegalArgumentException("RDATA is null");
        }
        if (offset < 0 || offset + 3 >= rdata.length) {
            throw new IllegalArgumentException("RDATA offset out of bounds");
        }
        return ((rdata[offset] & 0xFFL) << 24)
                | ((rdata[offset + 1] & 0xFFL) << 16)
                | ((rdata[offset + 2] & 0xFFL) << 8)
                | (rdata[offset + 3] & 0xFFL);
    }

    private static void writeU32(byte[] out, int offset, long value) {
        out[offset] = (byte) ((value >> 24) & 0xFF);
        out[offset + 1] = (byte) ((value >> 16) & 0xFF);
        out[offset + 2] = (byte) ((value >> 8) & 0xFF);
        out[offset + 3] = (byte) (value & 0xFF);
    }

    private static void validateU32(long value, String field) {
        if (value < 0 || value > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("SOA " + field + " must be between 0 and 4294967295");
        }
    }
}
