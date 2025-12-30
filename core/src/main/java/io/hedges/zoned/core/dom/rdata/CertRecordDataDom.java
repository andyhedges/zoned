// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CertRecordDataDom implements RDataDom {
    private int certificateType;
    private int keyTag;
    private int algorithm;
    private byte[] certificate;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length < 5) {
            throw new IllegalArgumentException("CERT RDATA requires type, key tag, algorithm, and certificate");
        }
        int certificateType = RDataUtils.readU16(rdata, 0);
        int keyTag = RDataUtils.readU16(rdata, 2);
        int algorithm = RDataUtils.readU8(rdata, 4);
        int certLength = rdata.length - 5;
        if (certLength <= 0) {
            throw new IllegalArgumentException("CERT certificate must not be empty");
        }
        byte[] certificate = new byte[certLength];
        System.arraycopy(rdata, 5, certificate, 0, certLength);
        return CertRecordDataDom.builder()
                .certificateType(certificateType)
                .keyTag(keyTag)
                .algorithm(algorithm)
                .certificate(certificate)
                .build();
    }

    @Override
    public byte[] to() {
        if (certificateType < 0 || certificateType > 0xFFFF) {
            throw new IllegalArgumentException("CERT type must be between 0 and 65535");
        }
        if (keyTag < 0 || keyTag > 0xFFFF) {
            throw new IllegalArgumentException("CERT key tag must be between 0 and 65535");
        }
        if (algorithm < 0 || algorithm > 0xFF) {
            throw new IllegalArgumentException("CERT algorithm must be between 0 and 255");
        }
        if (certificate == null || certificate.length == 0) {
            throw new IllegalArgumentException("CERT certificate must not be empty");
        }
        byte[] out = new byte[5 + certificate.length];
        out[0] = (byte) ((certificateType >> 8) & 0xFF);
        out[1] = (byte) (certificateType & 0xFF);
        out[2] = (byte) ((keyTag >> 8) & 0xFF);
        out[3] = (byte) (keyTag & 0xFF);
        out[4] = (byte) (algorithm & 0xFF);
        System.arraycopy(certificate, 0, out, 5, certificate.length);
        return out;
    }
}
