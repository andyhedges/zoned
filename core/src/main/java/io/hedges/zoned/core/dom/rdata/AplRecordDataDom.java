// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for DNS APL record RDATA.
 *
 * <p>RDATA is a sequence of address-prefix items:
 * address family (16-bit), prefix length (8-bit), length/negation (8-bit),
 * followed by address bytes.</p>
 
 *
 * <pre>
 * +---------------+--------------+----------------------------------+
 * | Field         | Size (octets)| Description                      |
 * +---------------+--------------+----------------------------------+
 * | Family        | 2            | Address family.                  |
 * | PrefixLength  | 1            | Prefix length.                   |
 * | Length/Neg    | 1            | Negation flag and address length.|
 * | Address       | variable     | Address bytes (per item).        |
 * +---------------+--------------+----------------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class AplRecordDataDom implements RDataDom {
    private List<AplEntry> entries;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null) {
            throw new IllegalArgumentException("APL RDATA cannot be null");
        }
        if (rdata.length < 4) {
            throw new IllegalArgumentException("APL RDATA must be at least 4 bytes");
        }
        int idx = 0;
        List<AplEntry> entries = new ArrayList<>();
        while (idx < rdata.length) {
            if (rdata.length - idx < 4) {
                throw new IllegalArgumentException("APL entry header truncated");
            }
            int addressFamily = RDataUtils.readU16(rdata, idx);
            int prefixLength = RDataUtils.readU8(rdata, idx + 2);
            int lengthByte = RDataUtils.readU8(rdata, idx + 3);
            boolean negation = (lengthByte & 0x80) != 0;
            int addressLength = lengthByte & 0x7F;
            idx += 4;
            if (idx + addressLength > rdata.length) {
                throw new IllegalArgumentException("APL entry address exceeds RDATA length");
            }
            byte[] address = new byte[addressLength];
            System.arraycopy(rdata, idx, address, 0, addressLength);
            idx += addressLength;
            validateEntry(addressFamily, prefixLength, address);
            entries.add(AplEntry.builder()
                    .addressFamily(addressFamily)
                    .prefixLength(prefixLength)
                    .negation(negation)
                    .address(address)
                    .build());
        }
        return AplRecordDataDom.builder().entries(entries).build();
    }

    @Override
    public byte[] to() {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("APL RDATA requires at least one entry");
        }
        int size = 0;
        for (int i = 0; i < entries.size(); i++) {
            AplEntry entry = entries.get(i);
            if (entry == null) {
                throw new IllegalArgumentException("APL entry[" + i + "] is null");
            }
            byte[] address = entry.address();
            if (address == null) {
                throw new IllegalArgumentException("APL entry[" + i + "] address is null");
            }
            if (address.length > 0x7F) {
                throw new IllegalArgumentException("APL entry[" + i + "] address length exceeds 127 bytes");
            }
            validateEntry(entry.addressFamily(), entry.prefixLength(), address);
            size += 4 + address.length;
        }
        byte[] out = new byte[size];
        int idx = 0;
        for (AplEntry entry : entries) {
            int addressFamily = entry.addressFamily();
            int prefixLength = entry.prefixLength();
            byte[] address = entry.address();
            out[idx++] = (byte) ((addressFamily >> 8) & 0xFF);
            out[idx++] = (byte) (addressFamily & 0xFF);
            out[idx++] = (byte) (prefixLength & 0xFF);
            int lengthByte = address.length & 0x7F;
            if (entry.negation()) {
                lengthByte |= 0x80;
            }
            out[idx++] = (byte) lengthByte;
            System.arraycopy(address, 0, out, idx, address.length);
            idx += address.length;
        }
        return out;
    }

    private static void validateEntry(int addressFamily, int prefixLength, byte[] address) {
        if (addressFamily < 0 || addressFamily > 0xFFFF) {
            throw new IllegalArgumentException("APL address family must be between 0 and 65535");
        }
        if (prefixLength < 0 || prefixLength > 0xFF) {
            throw new IllegalArgumentException("APL prefix length must be between 0 and 255");
        }
        if (address == null) {
            throw new IllegalArgumentException("APL entry address is null");
        }
        if (address.length > 0x7F) {
            throw new IllegalArgumentException("APL entry address length exceeds 127 bytes");
        }
        if (address.length * 8 < prefixLength) {
            throw new IllegalArgumentException("APL entry address length is too short for prefix");
        }
        if (addressFamily == 1) {
            if (prefixLength > 32) {
                throw new IllegalArgumentException("APL IPv4 prefix length must be between 0 and 32");
            }
            if (address.length > 4) {
                throw new IllegalArgumentException("APL IPv4 address length must be between 0 and 4 bytes");
            }
        } else if (addressFamily == 2) {
            if (prefixLength > 128) {
                throw new IllegalArgumentException("APL IPv6 prefix length must be between 0 and 128");
            }
            if (address.length > 16) {
                throw new IllegalArgumentException("APL IPv6 address length must be between 0 and 16 bytes");
            }
        }
    }

    @Getter
    @Builder
    @ToString
    public static class AplEntry {
        private int addressFamily;
        private int prefixLength;
        private boolean negation;
        private byte[] address;
    }
}
