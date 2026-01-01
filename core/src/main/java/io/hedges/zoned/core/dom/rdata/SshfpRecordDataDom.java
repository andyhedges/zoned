// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS SSHFP record RDATA.
 *
 * <p>RDATA is algorithm (8-bit), fingerprint type (8-bit), and fingerprint bytes.</p>
 
 *
 * <pre>
 * +-------------+--------------+----------------------------+
 * | Field       | Size (octets)| Description                |
 * +-------------+--------------+----------------------------+
 * | Algorithm   | 1            | SSH key algorithm.         |
 * | FpType      | 1            | Fingerprint type.          |
 * | Fingerprint | variable     | Fingerprint bytes.         |
 * +-------------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes Secure Shell (SSH) public key fingerprints for SSH host verification.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc4255">RFC 4255</a>.</p>*/
@Getter
@Builder
@ToString
public class SshfpRecordDataDom implements RDataDom {
    private int algorithm;
    private int fingerprintType;
    private byte[] fingerprint;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length < 3) {
            throw new IllegalArgumentException("SSHFP RDATA requires algorithm, fingerprint type, and data");
        }
        int algorithm = RDataUtils.readU8(rdata, 0);
        int fingerprintType = RDataUtils.readU8(rdata, 1);
        int fingerprintLength = rdata.length - 2;
        if (fingerprintLength <= 0) {
            throw new IllegalArgumentException("SSHFP fingerprint must not be empty");
        }
        byte[] fingerprint = new byte[fingerprintLength];
        System.arraycopy(rdata, 2, fingerprint, 0, fingerprintLength);
        return SshfpRecordDataDom.builder()
                .algorithm(algorithm)
                .fingerprintType(fingerprintType)
                .fingerprint(fingerprint)
                .build();
    }

    @Override
    public byte[] to() {
        if (algorithm < 0 || algorithm > 0xFF) {
            throw new IllegalArgumentException("SSHFP algorithm must be between 0 and 255");
        }
        if (fingerprintType < 0 || fingerprintType > 0xFF) {
            throw new IllegalArgumentException("SSHFP fingerprint type must be between 0 and 255");
        }
        if (fingerprint == null || fingerprint.length == 0) {
            throw new IllegalArgumentException("SSHFP fingerprint must not be empty");
        }
        byte[] out = new byte[2 + fingerprint.length];
        out[0] = (byte) (algorithm & 0xFF);
        out[1] = (byte) (fingerprintType & 0xFF);
        System.arraycopy(fingerprint, 0, out, 2, fingerprint.length);
        return out;
    }
}
