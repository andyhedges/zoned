// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SshfpRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> SshfpRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> SshfpRecordDataDom.from(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> SshfpRecordDataDom.from(new byte[1]));
        assertThrows(IllegalArgumentException.class, () -> SshfpRecordDataDom.from(new byte[2]));
    }

    @Test
    void fromRejectsEmptyFingerprint() {
        byte[] rdata = new byte[] {1, 1};
        assertThrows(IllegalArgumentException.class, () -> SshfpRecordDataDom.from(rdata));
    }

    @Test
    void fromParsesFields() {
        byte[] fingerprint = new byte[] {1, 2, 3, 4};
        byte[] rdata = new byte[] {1, 2, 1, 2, 3, 4};

        RDataDom dom = SshfpRecordDataDom.from(rdata);
        SshfpRecordDataDom sshfp = assertInstanceOf(SshfpRecordDataDom.class, dom);

        assertEquals(1, sshfp.algorithm());
        assertEquals(2, sshfp.fingerprintType());
        assertArrayEquals(fingerprint, sshfp.fingerprint());
    }

    @Test
    void toRejectsInvalidFields() {
        SshfpRecordDataDom missingFingerprint = SshfpRecordDataDom.builder()
                .algorithm(1)
                .fingerprintType(1)
                .build();
        SshfpRecordDataDom emptyFingerprint = SshfpRecordDataDom.builder()
                .algorithm(1)
                .fingerprintType(1)
                .fingerprint(new byte[0])
                .build();
        SshfpRecordDataDom negativeAlgorithm = SshfpRecordDataDom.builder()
                .algorithm(-1)
                .fingerprintType(1)
                .fingerprint(new byte[] {1})
                .build();
        SshfpRecordDataDom tooLargeAlgorithm = SshfpRecordDataDom.builder()
                .algorithm(256)
                .fingerprintType(1)
                .fingerprint(new byte[] {1})
                .build();
        SshfpRecordDataDom negativeType = SshfpRecordDataDom.builder()
                .algorithm(1)
                .fingerprintType(-1)
                .fingerprint(new byte[] {1})
                .build();
        SshfpRecordDataDom tooLargeType = SshfpRecordDataDom.builder()
                .algorithm(1)
                .fingerprintType(256)
                .fingerprint(new byte[] {1})
                .build();

        assertThrows(IllegalArgumentException.class, missingFingerprint::to);
        assertThrows(IllegalArgumentException.class, emptyFingerprint::to);
        assertThrows(IllegalArgumentException.class, negativeAlgorithm::to);
        assertThrows(IllegalArgumentException.class, tooLargeAlgorithm::to);
        assertThrows(IllegalArgumentException.class, negativeType::to);
        assertThrows(IllegalArgumentException.class, tooLargeType::to);
    }

    @Test
    void toSerializesRdata() {
        byte[] fingerprint = new byte[] {9, 8, 7, 6};
        SshfpRecordDataDom dom = SshfpRecordDataDom.builder()
                .algorithm(2)
                .fingerprintType(3)
                .fingerprint(fingerprint)
                .build();

        byte[] expected = new byte[] {2, 3, 9, 8, 7, 6};
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        byte[] fingerprint = new byte[] {5, 4, 3};
        SshfpRecordDataDom original = SshfpRecordDataDom.builder()
                .algorithm(1)
                .fingerprintType(2)
                .fingerprint(fingerprint)
                .build();

        RDataDom decoded = SshfpRecordDataDom.from(original.to());
        SshfpRecordDataDom parsed = assertInstanceOf(SshfpRecordDataDom.class, decoded);

        assertEquals(1, parsed.algorithm());
        assertEquals(2, parsed.fingerprintType());
        assertArrayEquals(fingerprint, parsed.fingerprint());
    }
}
