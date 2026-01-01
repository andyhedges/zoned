// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;

/**
 * Domain model for DNS NAPTR record RDATA.
 *
 * <p>RDATA is order (16-bit), preference (16-bit), flags (character-string),
 * services (character-string), regexp (character-string), and replacement name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Order     | 2            | NAPTR order.               |
 * | Preference| 2            | NAPTR preference.          |
 * | Flags     | variable     | Flags character-string.    |
 * | Services  | variable     | Services character-string. |
 * | Regexp    | variable     | Regexp character-string.   |
 * | Replacement| variable    | Replacement name.          |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Provides Naming Authority Pointer (NAPTR) rules for rewriting names for services like SIP and ENUM.</p>
 * <p>NAPTR is often used in multi-step resolution workflows, where order and preference guide which
 * rewrite or service rule to apply first.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc3403">RFC 3403</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class NaptrRecordDataDom implements RDataDom {
    private int order;
    private int preference;
    private String flags;
    private String services;
    private String regexp;
    private DnsNameDom replacement;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        if (rdata == null || rdata.length < 7) {
            throw new IllegalArgumentException("NAPTR RDATA requires order, preference, and fields");
        }
        int order = RDataUtils.readU16(rdata, 0);
        int preference = RDataUtils.readU16(rdata, 2);
        int idx = 4;
        CharacterStringRead flagsRead = readCharacterString(rdata, idx, "flags");
        idx += flagsRead.totalLength();
        CharacterStringRead servicesRead = readCharacterString(rdata, idx, "services");
        idx += servicesRead.totalLength();
        CharacterStringRead regexpRead = readCharacterString(rdata, idx, "regexp");
        idx += regexpRead.totalLength();
        if (idx >= rdata.length) {
            throw new IllegalArgumentException("NAPTR replacement name is missing");
        }
        byte[] nameBytes = new byte[rdata.length - idx];
        System.arraycopy(rdata, idx, nameBytes, 0, nameBytes.length);
        DnsNameDom replacement = RDataUtils.toDnsNameDom(nameBytes, resolver);
        return NaptrRecordDataDom.builder()
                .order(order)
                .preference(preference)
                .flags(flagsRead.value())
                .services(servicesRead.value())
                .regexp(regexpRead.value())
                .replacement(replacement)
                .build();
    }

    @Override
    public byte[] to() {
        if (order < 0 || order > 0xFFFF) {
            throw new IllegalArgumentException("NAPTR order must be between 0 and 65535");
        }
        if (preference < 0 || preference > 0xFFFF) {
            throw new IllegalArgumentException("NAPTR preference must be between 0 and 65535");
        }
        if (flags == null || services == null || regexp == null) {
            throw new IllegalArgumentException("NAPTR flags, services, and regexp must not be null");
        }
        byte[] flagsBytes = toCharacterString(flags, "flags");
        byte[] servicesBytes = toCharacterString(services, "services");
        byte[] regexpBytes = toCharacterString(regexp, "regexp");
        if (replacement == null) {
            throw new IllegalArgumentException("NAPTR replacement name is null");
        }
        byte[] nameBytes = RDataUtils.toByteArray(replacement);
        int total = 4 + flagsBytes.length + servicesBytes.length + regexpBytes.length + nameBytes.length;
        byte[] out = new byte[total];
        int idx = 0;
        out[idx++] = (byte) ((order >> 8) & 0xFF);
        out[idx++] = (byte) (order & 0xFF);
        out[idx++] = (byte) ((preference >> 8) & 0xFF);
        out[idx++] = (byte) (preference & 0xFF);
        System.arraycopy(flagsBytes, 0, out, idx, flagsBytes.length);
        idx += flagsBytes.length;
        System.arraycopy(servicesBytes, 0, out, idx, servicesBytes.length);
        idx += servicesBytes.length;
        System.arraycopy(regexpBytes, 0, out, idx, regexpBytes.length);
        idx += regexpBytes.length;
        System.arraycopy(nameBytes, 0, out, idx, nameBytes.length);
        return out;
    }

    private static CharacterStringRead readCharacterString(byte[] rdata, int offset, String field) {
        if (offset >= rdata.length) {
            throw new IllegalArgumentException("NAPTR " + field + " is missing");
        }
        int len = RDataUtils.readU8(rdata, offset);
        int start = offset + 1;
        int end = start + len;
        if (end > rdata.length) {
            throw new IllegalArgumentException("NAPTR " + field + " exceeds RDATA length");
        }
        return new CharacterStringRead(new String(rdata, start, len, StandardCharsets.US_ASCII), 1 + len);
    }

    private static byte[] toCharacterString(String value, String field) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length > 0xFF) {
            throw new IllegalArgumentException("NAPTR " + field + " exceeds 255 bytes");
        }
        byte[] out = new byte[1 + bytes.length];
        out[0] = (byte) (bytes.length & 0xFF);
        System.arraycopy(bytes, 0, out, 1, bytes.length);
        return out;
    }

    private record CharacterStringRead(String value, int totalLength) {
    }
}
