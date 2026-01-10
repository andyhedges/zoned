// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsNameDomPolicy;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for DNS HIP record RDATA.
 *
 * <p>RDATA is HIT length (8-bit), algorithm (8-bit), key length (16-bit),
 * HIT, public key, and optional rendezvous server names.</p>
 *
 * <pre>
 * +---------------+--------------+----------------------------------------------+
 * | Field         | Size (octets)| Description                                  |
 * +---------------+--------------+----------------------------------------------+
 * | HITLength     | 1            | Length of HIT in octets.                     |
 * | Algorithm     | 1            | Public key algorithm.                        |
 * | KeyLength     | 2            | Length of public key in octets.              |
 * | HIT           | variable     | Host Identity Tag bytes.                     |
 * | PublicKey     | variable     | Public key bytes.                            |
 * | Rendezvous    | variable     | Zero or more domain names (wire format).     |
 * +---------------+--------------+----------------------------------------------+
 * </pre>
 
 * <p>Purpose: Publishes Host Identity Protocol (HIP) data including the Host Identity Tag (HIT), public key, and optional rendezvous servers.</p>
 * <p>HIP separates identity from location, and these records let clients discover the cryptographic identity
 * and any rendezvous servers used to reach a mobile or multi-homed host.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc8005">RFC 8005</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class HipRecordDataDom implements RDataDom {
    private byte[] hit;
    private int algorithm;
    private byte[] publicKey;
    private List<DnsNameDom> rendezvousServers;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        if (rdata == null || rdata.length < 5) {
            throw new IllegalArgumentException("HIP RDATA requires HIT length, algorithm, and key length");
        }
        int hitLength = RDataUtils.readU8(rdata, 0);
        int algorithm = RDataUtils.readU8(rdata, 1);
        int keyLength = RDataUtils.readU16(rdata, 2);
        if (hitLength <= 0) {
            throw new IllegalArgumentException("HIP HIT length must be at least 1");
        }
        if (keyLength <= 0) {
            throw new IllegalArgumentException("HIP public key length must be at least 1");
        }
        int idx = 4;
        if (idx + hitLength + keyLength > rdata.length) {
            throw new IllegalArgumentException("HIP RDATA length is too short for HIT and key");
        }
        byte[] hit = new byte[hitLength];
        System.arraycopy(rdata, idx, hit, 0, hitLength);
        idx += hitLength;
        byte[] publicKey = new byte[keyLength];
        System.arraycopy(rdata, idx, publicKey, 0, keyLength);
        idx += keyLength;

        List<DnsNameDom> rendezvousServers = new ArrayList<>();
        while (idx < rdata.length) {
            RDataUtils.DnsNameParseResult parsed = RDataUtils.parseDnsName(
                    rdata,
                    idx,
                    resolver,
                    DnsNameDomPolicy.Builtin.HOSTNAME);
            rendezvousServers.add(parsed.name());
            idx = parsed.nextIndex();
        }

        return HipRecordDataDom.builder()
                .hit(hit)
                .algorithm(algorithm)
                .publicKey(publicKey)
                .rendezvousServers(rendezvousServers)
                .build();
    }

    @Override
    public byte[] to() {
        if (hit == null || hit.length == 0) {
            throw new IllegalArgumentException("HIP HIT must not be empty");
        }
        if (hit.length > 0xFF) {
            throw new IllegalArgumentException("HIP HIT length must be between 1 and 255");
        }
        if (algorithm < 0 || algorithm > 0xFF) {
            throw new IllegalArgumentException("HIP algorithm must be between 0 and 255");
        }
        if (publicKey == null || publicKey.length == 0) {
            throw new IllegalArgumentException("HIP public key must not be empty");
        }
        if (publicKey.length > 0xFFFF) {
            throw new IllegalArgumentException("HIP public key length must be between 1 and 65535");
        }
        if (rendezvousServers == null) {
            throw new IllegalArgumentException("HIP rendezvous server list is null");
        }

        List<byte[]> nameBytes = new ArrayList<>(rendezvousServers.size());
        int nameBytesSize = 0;
        for (int i = 0; i < rendezvousServers.size(); i++) {
            DnsNameDom name = rendezvousServers.get(i);
            if (name == null) {
                throw new IllegalArgumentException("HIP rendezvous server[" + i + "] is null");
            }
            byte[] encoded = RDataUtils.toByteArray(name);
            nameBytes.add(encoded);
            nameBytesSize += encoded.length;
        }

        byte[] out = new byte[4 + hit.length + publicKey.length + nameBytesSize];
        int idx = 0;
        out[idx++] = (byte) (hit.length & 0xFF);
        out[idx++] = (byte) (algorithm & 0xFF);
        out[idx++] = (byte) ((publicKey.length >> 8) & 0xFF);
        out[idx++] = (byte) (publicKey.length & 0xFF);
        System.arraycopy(hit, 0, out, idx, hit.length);
        idx += hit.length;
        System.arraycopy(publicKey, 0, out, idx, publicKey.length);
        idx += publicKey.length;
        for (byte[] encoded : nameBytes) {
            System.arraycopy(encoded, 0, out, idx, encoded.length);
            idx += encoded.length;
        }
        return out;
    }
}
