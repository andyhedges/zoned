// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@ToString

/**
 * Domain model for DNS TXT record RDATA.
 *
 * <p>This class deliberately avoids imposing any character encoding on TXT
 * record {@code character-string} values. At the DNS protocol level, TXT
 * RDATA is defined as one or more {@code character-string} elements, each
 * consisting of a length octet followed by raw bytes.</p>
 *
 * <p>RFC&nbsp;1035 explicitly treats {@code character-string} values as
 * binary information and does not define a character set or encoding for
 * TXT records. Any interpretation of these bytes (for example, ASCII or
 * UTF-8) is therefore the responsibility of higher-level protocols or
 * application code, not DNS itself.</p>
 *
 * <p>See
 * <a href="https://www.rfc-editor.org/rfc/rfc1035.html#section-3.3">
 * RFC&nbsp;1035&nbsp;§3.3 (Character strings)
 * </a>
 * and
 * <a href="https://www.rfc-editor.org/rfc/rfc1035.html#section-3.3.14">
 * RFC&nbsp;1035&nbsp;§3.3.14 (TXT RDATA format)
 * </a>
 * for the normative definitions.</p>
 
 *
 * <pre>
 * +----------------+--------------+----------------------------+
 * | Field          | Size (octets)| Description                |
 * +----------------+--------------+----------------------------+
 * | CharacterString| variable     | Length-prefixed bytes.     |
 * +----------------+--------------+----------------------------+
 * </pre>
*/
public class TxtRecordDataDom implements RDataDom {

    private List<byte[]> characterStrings;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length <= 2) {
            throw new IllegalArgumentException("TXT RDATA cannot be empty or shorter than 2 bytes");
        }

        //Shouldn't be able to get here as the byte should be produced by code that read a short
        //to know the length of the rdata, however a guard doesn't cost much
        if (rdata.length > 0xFFFF) {
            throw new IllegalArgumentException("TXT RDATA exceeds 65535 bytes");
        }

        int idx = 0;
        List<byte[]> characterStrings = new ArrayList<>();
        while (idx < rdata.length) {
            int len = rdata[idx++] & 0xFF;
            if (len + idx > rdata.length) {
                throw new IllegalArgumentException("TXT attempted to read passed the RDATA section");
            }
            byte[] chunk = new byte[len];
            System.arraycopy(rdata, idx, chunk, 0, len);
            characterStrings.add(chunk);
            idx += len;
        }
        return TxtRecordDataDom.builder().characterStrings(characterStrings).build();

    }

    @Override
    public byte[] to() {
        if (characterStrings == null || characterStrings.isEmpty()) {
            throw new IllegalArgumentException("TXT RDATA requires at least one character-string");
        }
        int size = characterStrings.size(); //allow for all the length bytes
        for (int i = 0; i < characterStrings.size(); i++) {
            byte[] s = characterStrings.get(i);
            if (s == null) {
                throw new IllegalArgumentException("TXT RDATA character-string[" + i + "] is null");
            }
            if (s.length > 0xFF) {
                throw new IllegalArgumentException("TXT RDATA character-string[" + i + "] exceeds 255 bytes");
            }
            if (size + s.length > 0xFFFF) {
                throw new IllegalArgumentException("TXT RDATA exceeds 65535 bytes");
            }
            size += s.length;

        }
        byte[] out = new byte[size];
        int idx = 0;
        for (byte[] s : characterStrings) {
            out[idx++] = (byte) (s.length & 0xFF);
            System.arraycopy(s, 0, out, idx, s.length);
            idx += s.length;
        }
        // as far as I can tell the code above makes this impossible but if it's changed
        // or if someone finds and exploit this is belt and braces
        if (idx != out.length) {
            throw new IllegalStateException("Bug: TXT RDATA encoded length mismatch");
        }
        return out;
    }
}
