// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RDataUtils {

    public static final int COMPRESSION_POINTER_MARKER = 0xC0;

    protected static int readU16(byte[] rdata, int offset) {
        if (rdata == null) {
            throw new IllegalArgumentException("RDATA is null");
        }
        if (offset < 0 || offset + 1 >= rdata.length) {
            throw new IllegalArgumentException("RDATA offset out of bounds");
        }
        return ((rdata[offset] & 0xFF) << 8) | (rdata[offset + 1] & 0xFF);
    }

    protected static int readU8(byte[] rdata, int offset) {
        if (rdata == null) {
            throw new IllegalArgumentException("RDATA is null");
        }
        if (offset < 0 || offset >= rdata.length) {
            throw new IllegalArgumentException("RDATA offset out of bounds");
        }
        return rdata[offset] & 0xFF;
    }

    protected static long readU32(byte[] rdata, int offset) {
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

    protected static void writeU32(byte[] out, int offset, long value) {
        out[offset] = (byte) ((value >> 24) & 0xFF);
        out[offset + 1] = (byte) ((value >> 16) & 0xFF);
        out[offset + 2] = (byte) ((value >> 8) & 0xFF);
        out[offset + 3] = (byte) (value & 0xFF);
    }

    protected static Inet6Address toInet6Address(byte[] rdata) {
        if (rdata == null || rdata.length != 16) {
            throw new IllegalArgumentException("AAAA RDATA must be exactly 16 bytes");
        }
        try {
            InetAddress addr = InetAddress.getByAddress(rdata);
            if (!(addr instanceof Inet6Address)) {
                throw new IllegalStateException("Expected Inet6Address, got " + addr.getClass().getSimpleName());
            }
            return (Inet6Address) addr;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPv6 address bytes: " + Arrays.toString(rdata), e);
        }
    }

    protected static byte[] toByteArray(Inet6Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Inet6Address is null");
        }
        byte[] raw = address.getAddress();
        byte[] rdata = new byte[16];
        System.arraycopy(raw, 0, rdata, 0, 16);
        return rdata;
    }

    protected static Inet4Address toInet4Address(byte[] rdata) {
        if (rdata == null || rdata.length != 4) {
            throw new IllegalArgumentException("A RDATA must be exactly 4 bytes");
        }
        try {
            InetAddress addr = InetAddress.getByAddress(rdata);
            if (!(addr instanceof Inet4Address)) {
                throw new IllegalStateException("Expected Inet4Address, got " + addr.getClass().getSimpleName());
            }
            return (Inet4Address) addr;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPv4 bytes: " + Arrays.toString(rdata), e);
        }
    }

    protected static byte[] toByteArray(Inet4Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Inet4Address is null");
        }
        byte[] raw = address.getAddress();
        byte[] rdata = new byte[4];
        System.arraycopy(raw, 0, rdata, 0, 4);
        return rdata;
    }

    protected static DnsNameDom toDnsNameDom(byte[] rdata) {
        return toDnsNameDom(rdata, null);
    }

    protected static DnsNameDom toDnsNameDom(byte[] rdata, NameResolver resolver) {
        DnsNameParseResult parsed = parseDnsName(rdata, 0, resolver);
        if (parsed.nextIndex() != rdata.length) {
            throw new IllegalArgumentException("Extra bytes after RDATA name");
        }
        return parsed.name();
    }

    protected static DnsNameParseResult parseDnsName(byte[] rdata, int offset, NameResolver resolver) {
        if (rdata == null || rdata.length == 0) {
            throw new IllegalArgumentException("Name RDATA cannot be empty");
        }

        if (offset < 0 || offset >= rdata.length) {
            throw new IllegalArgumentException("Name RDATA offset out of bounds");
        }

        if (rdata.length - offset > 255) {
            throw new IllegalArgumentException("DNS name exceeds 255 bytes");
        }

        List<String> decoded = new ArrayList<>();
        int idx = offset;

        while (idx < rdata.length) {
            int len = rdata[idx] & 0xFF;
            if ((len & COMPRESSION_POINTER_MARKER) == COMPRESSION_POINTER_MARKER) {
                if (idx + 1 >= rdata.length) {
                    throw new IllegalArgumentException("Truncated compression pointer");
                }
                if (resolver == null) {
                    throw new UnsupportedOperationException("Compressed name encountered but no NameResolver provided");
                }
                int pointer = ((len & 0x3F) << 8) | (rdata[idx + 1] & 0xFF);
                DnsNameDom resolved = resolver.resolve(pointer);
                if (resolved == null || resolved.labels() == null) {
                    throw new IllegalArgumentException("Resolved name is null");
                }
                decoded.addAll(resolved.labels());
                idx += 2;
                break;
            }
            idx++;

            if (len > 63) {
                throw new IllegalArgumentException("Label length exceeds 63 bytes");
            }

            if (len == 0) {
                // end of name
                break;
            }

            if (idx + len > rdata.length) {
                throw new IllegalArgumentException("Label length " + len + " exceeds RDATA bounds");
            }

            String label = new String(rdata, idx, len, StandardCharsets.US_ASCII);
            decoded.add(label);
            idx += len;
        }

        if (decoded.isEmpty()) {
            throw new IllegalArgumentException("Name must have at least one label");
        }

        return new DnsNameParseResult(DnsNameDom.builder().labels(decoded).build(), idx);
    }

    protected static byte[] toByteArray(DnsNameDom name) {
        if (name == null) {
            throw new IllegalArgumentException("CNAME is null");
        }
        if (name.labels() == null) {
            throw new IllegalArgumentException("Labels list is null");
        }
        if (name.labels().isEmpty()) {
            throw new IllegalArgumentException("CNAME must have at least one label");
        }

        // Compute total length
        int totalLength = 1; // final zero byte
        for (String label : name.labels()) {
            byte[] b = label.getBytes(StandardCharsets.US_ASCII);
            if (b.length > 63) {
                throw new IllegalArgumentException("Label exceeds 63 bytes: " + label);
            }
            totalLength += 1 + b.length;
            if (totalLength > 255) {
                throw new IllegalArgumentException("DNS Name exceeds 255 bytes");
            }
        }

        byte[] rdata = new byte[totalLength];
        int idx = 0;

        for (String label : name.labels()) {
            byte[] b = label.getBytes(StandardCharsets.US_ASCII);
            rdata[idx++] = (byte) b.length;
            System.arraycopy(b, 0, rdata, idx, b.length);
            idx += b.length;
        }

        rdata[idx] = 0; // terminating root label

        return rdata;
    }

    protected static final class DnsNameParseResult {
        private final DnsNameDom name;
        private final int nextIndex;

        private DnsNameParseResult(DnsNameDom name, int nextIndex) {
            this.name = name;
            this.nextIndex = nextIndex;
        }

        protected DnsNameDom name() {
            return name;
        }

        protected int nextIndex() {
            return nextIndex;
        }
    }
}
