// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS IPSECKEY record RDATA.
 *
 * <p>RDATA is precedence (8-bit), gateway type (8-bit), algorithm (8-bit),
 * gateway (variable), and public key (variable).</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Precedence| 1            | Gateway precedence.        |
 * | GatewayTy | 1            | Gateway type.              |
 * | Algorithm | 1            | Public key algorithm.      |
 * | Gateway   | variable     | Gateway bytes or name.     |
 * | PublicKey | variable     | Public key bytes.          |
 * +-----------+--------------+----------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class IpseckeyRecordDataDom implements RDataDom {
    private int precedence;
    private int gatewayType;
    private int algorithm;
    private byte[] gateway;
    private byte[] publicKey;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length < 3) {
            throw new IllegalArgumentException("IPSECKEY RDATA requires precedence, gateway type, and algorithm");
        }
        int precedence = RDataUtils.readU8(rdata, 0);
        int gatewayType = RDataUtils.readU8(rdata, 1);
        int algorithm = RDataUtils.readU8(rdata, 2);
        int idx = 3;
        byte[] gateway = switch (gatewayType) {
            case 0 -> new byte[0];
            case 1 -> {
                if (rdata.length < idx + 4) {
                    throw new IllegalArgumentException("IPSECKEY IPv4 gateway is truncated");
                }
                byte[] bytes = new byte[4];
                System.arraycopy(rdata, idx, bytes, 0, 4);
                idx += 4;
                yield bytes;
            }
            case 2 -> {
                if (rdata.length < idx + 16) {
                    throw new IllegalArgumentException("IPSECKEY IPv6 gateway is truncated");
                }
                byte[] bytes = new byte[16];
                System.arraycopy(rdata, idx, bytes, 0, 16);
                idx += 16;
                yield bytes;
            }
            case 3 -> {
                RDataUtils.DnsNameParseResult parsed = RDataUtils.parseDnsName(rdata, idx, null);
                int nameEnd = parsed.nextIndex();
                byte[] nameBytes = new byte[nameEnd - idx];
                System.arraycopy(rdata, idx, nameBytes, 0, nameBytes.length);
                idx = nameEnd;
                yield nameBytes;
            }
            default -> throw new IllegalArgumentException("IPSECKEY gateway type must be between 0 and 3");
        };
        if (idx > rdata.length) {
            throw new IllegalArgumentException("IPSECKEY gateway exceeds RDATA length");
        }
        byte[] publicKey = new byte[rdata.length - idx];
        if (publicKey.length > 0) {
            System.arraycopy(rdata, idx, publicKey, 0, publicKey.length);
        }
        return IpseckeyRecordDataDom.builder()
                .precedence(precedence)
                .gatewayType(gatewayType)
                .algorithm(algorithm)
                .gateway(gateway)
                .publicKey(publicKey)
                .build();
    }

    @Override
    public byte[] to() {
        if (precedence < 0 || precedence > 0xFF) {
            throw new IllegalArgumentException("IPSECKEY precedence must be between 0 and 255");
        }
        if (gatewayType < 0 || gatewayType > 3) {
            throw new IllegalArgumentException("IPSECKEY gateway type must be between 0 and 3");
        }
        if (algorithm < 0 || algorithm > 0xFF) {
            throw new IllegalArgumentException("IPSECKEY algorithm must be between 0 and 255");
        }
        if (gateway == null) {
            throw new IllegalArgumentException("IPSECKEY gateway is null");
        }
        int gatewayLength = switch (gatewayType) {
            case 0 -> 0;
            case 1 -> 4;
            case 2 -> 16;
            case 3 -> gateway.length;
            default -> 0;
        };
        if ((gatewayType == 1 && gateway.length != 4)
                || (gatewayType == 2 && gateway.length != 16)
                || (gatewayType == 0 && gateway.length != 0)) {
            throw new IllegalArgumentException("IPSECKEY gateway length does not match gateway type");
        }
        if (gatewayType == 3) {
            RDataUtils.toDnsNameDom(gateway);
            gatewayLength = gateway.length;
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("IPSECKEY public key is null");
        }
        byte[] out = new byte[3 + gatewayLength + publicKey.length];
        int idx = 0;
        out[idx++] = (byte) (precedence & 0xFF);
        out[idx++] = (byte) (gatewayType & 0xFF);
        out[idx++] = (byte) (algorithm & 0xFF);
        if (gatewayLength > 0) {
            System.arraycopy(gateway, 0, out, idx, gatewayLength);
            idx += gatewayLength;
        }
        if (publicKey.length > 0) {
            System.arraycopy(publicKey, 0, out, idx, publicKey.length);
        }
        return out;
    }
}
