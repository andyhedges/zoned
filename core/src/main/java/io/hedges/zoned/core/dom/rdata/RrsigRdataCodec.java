// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;

final class RrsigRdataCodec {

    private RrsigRdataCodec() {
    }

    static SigFields parse(byte[] rdata, NameResolver resolver, String typeLabel) {
        if (rdata == null || rdata.length < 19) {
            throw new IllegalArgumentException(typeLabel + " RDATA is too short");
        }
        int typeCovered = RDataUtils.readU16(rdata, 0);
        int algorithm = RDataUtils.readU8(rdata, 2);
        int labels = RDataUtils.readU8(rdata, 3);
        long originalTtl = RDataUtils.readU32(rdata, 4);
        long signatureExpiration = RDataUtils.readU32(rdata, 8);
        long signatureInception = RDataUtils.readU32(rdata, 12);
        int keyTag = RDataUtils.readU16(rdata, 16);
        int idx = 18;
        RDataUtils.DnsNameParseResult signerResult = RDataUtils.parseDnsName(rdata, idx, resolver);
        idx = signerResult.nextIndex();
        if (idx >= rdata.length) {
            throw new IllegalArgumentException(typeLabel + " signature is missing");
        }
        byte[] signature = new byte[rdata.length - idx];
        System.arraycopy(rdata, idx, signature, 0, signature.length);
        return new SigFields(
                typeCovered,
                algorithm,
                labels,
                originalTtl,
                signatureExpiration,
                signatureInception,
                keyTag,
                signerResult.name(),
                signature
        );
    }

    static byte[] encode(
            int typeCovered,
            int algorithm,
            int labels,
            long originalTtl,
            long signatureExpiration,
            long signatureInception,
            int keyTag,
            DnsNameDom signerName,
            byte[] signature,
            String typeLabel
    ) {
        validateRange(typeCovered, 0, 0xFFFF, typeLabel + " type covered");
        validateRange(algorithm, 0, 0xFF, typeLabel + " algorithm");
        validateRange(labels, 0, 0xFF, typeLabel + " labels");
        validateRange(originalTtl, 0, 0xFFFF_FFFFL, typeLabel + " original TTL");
        validateRange(signatureExpiration, 0, 0xFFFF_FFFFL, typeLabel + " signature expiration");
        validateRange(signatureInception, 0, 0xFFFF_FFFFL, typeLabel + " signature inception");
        validateRange(keyTag, 0, 0xFFFF, typeLabel + " key tag");
        if (signerName == null) {
            throw new IllegalArgumentException(typeLabel + " signer name is null");
        }
        if (signature == null || signature.length == 0) {
            throw new IllegalArgumentException(typeLabel + " signature must not be empty");
        }
        byte[] signerBytes = RDataUtils.toByteArray(signerName);
        byte[] out = new byte[18 + signerBytes.length + signature.length];
        out[0] = (byte) ((typeCovered >> 8) & 0xFF);
        out[1] = (byte) (typeCovered & 0xFF);
        out[2] = (byte) (algorithm & 0xFF);
        out[3] = (byte) (labels & 0xFF);
        RDataUtils.writeU32(out, 4, originalTtl);
        RDataUtils.writeU32(out, 8, signatureExpiration);
        RDataUtils.writeU32(out, 12, signatureInception);
        out[16] = (byte) ((keyTag >> 8) & 0xFF);
        out[17] = (byte) (keyTag & 0xFF);
        System.arraycopy(signerBytes, 0, out, 18, signerBytes.length);
        System.arraycopy(signature, 0, out, 18 + signerBytes.length, signature.length);
        return out;
    }

    private static void validateRange(long value, long min, long max, String label) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(label + " must be between " + min + " and " + max);
        }
    }

    record SigFields(
            int typeCovered,
            int algorithm,
            int labels,
            long originalTtl,
            long signatureExpiration,
            long signatureInception,
            int keyTag,
            DnsNameDom signerName,
            byte[] signature
    ) {
    }
}
