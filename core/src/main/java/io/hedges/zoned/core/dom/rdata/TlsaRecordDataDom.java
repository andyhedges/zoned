// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS TLSA record RDATA.
 *
 * <p>RDATA is usage (8-bit), selector (8-bit), matching type (8-bit),
 * and certificate association data.</p>
 
 *
 * <pre>
 * +-------------+--------------+----------------------------+
 * | Field       | Size (octets)| Description                |
 * +-------------+--------------+----------------------------+
 * | Usage       | 1            | Usage.                     |
 * | Selector    | 1            | Selector.                  |
 * | MatchType   | 1            | Matching type.             |
 * | AssocData   | variable     | Association data.          |
 * +-------------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes TLSA data that binds TLS certificates or public keys to a domain (DANE).</p>
 * <p>TLSA records are used with service endpoints (such as _port._proto names) to specify which
 * certificate or public key is acceptable, reducing reliance on external certificate authorities.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc6698">RFC 6698</a>.</p>*/
@Getter
@Builder
@ToString
public class TlsaRecordDataDom implements RDataDom {
    private int usage;
    private int selector;
    private int matchingType;
    private byte[] associationData;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length < 4) {
            throw new IllegalArgumentException("TLSA RDATA requires usage, selector, matching type, and data");
        }
        int usage = RDataUtils.readU8(rdata, 0);
        int selector = RDataUtils.readU8(rdata, 1);
        int matchingType = RDataUtils.readU8(rdata, 2);
        int dataLength = rdata.length - 3;
        if (dataLength <= 0) {
            throw new IllegalArgumentException("TLSA association data must not be empty");
        }
        validateAssociationLength(matchingType, dataLength);
        byte[] associationData = new byte[dataLength];
        System.arraycopy(rdata, 3, associationData, 0, dataLength);
        return TlsaRecordDataDom.builder()
                .usage(usage)
                .selector(selector)
                .matchingType(matchingType)
                .associationData(associationData)
                .build();
    }

    @Override
    public byte[] to() {
        if (usage < 0 || usage > 0xFF) {
            throw new IllegalArgumentException("TLSA usage must be between 0 and 255");
        }
        if (selector < 0 || selector > 0xFF) {
            throw new IllegalArgumentException("TLSA selector must be between 0 and 255");
        }
        if (matchingType < 0 || matchingType > 0xFF) {
            throw new IllegalArgumentException("TLSA matching type must be between 0 and 255");
        }
        if (associationData == null || associationData.length == 0) {
            throw new IllegalArgumentException("TLSA association data must not be empty");
        }
        validateAssociationLength(matchingType, associationData.length);
        byte[] out = new byte[3 + associationData.length];
        out[0] = (byte) (usage & 0xFF);
        out[1] = (byte) (selector & 0xFF);
        out[2] = (byte) (matchingType & 0xFF);
        System.arraycopy(associationData, 0, out, 3, associationData.length);
        return out;
    }

    private static void validateAssociationLength(int matchingType, int length) {
        if (matchingType == 1 && length != 32) {
            throw new IllegalArgumentException("TLSA association data must be 32 bytes for SHA-256");
        }
        if (matchingType == 2 && length != 64) {
            throw new IllegalArgumentException("TLSA association data must be 64 bytes for SHA-512");
        }
    }
}
