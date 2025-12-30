// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenPgpKeyRecordDataDomTest {

    @Test
    void fromRejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> OpenPgpKeyRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> OpenPgpKeyRecordDataDom.from(new byte[0]));
    }

    @Test
    void fromCopiesPublicKey() {
        byte[] rdata = new byte[] {1, 2, 3, 4};
        RDataDom dom = OpenPgpKeyRecordDataDom.from(rdata);
        OpenPgpKeyRecordDataDom key = assertInstanceOf(OpenPgpKeyRecordDataDom.class, dom);

        assertArrayEquals(rdata, key.publicKey());
    }

    @Test
    void toRejectsMissingKey() {
        OpenPgpKeyRecordDataDom missing = OpenPgpKeyRecordDataDom.builder().build();
        OpenPgpKeyRecordDataDom empty = OpenPgpKeyRecordDataDom.builder().publicKey(new byte[0]).build();

        assertThrows(IllegalArgumentException.class, missing::to);
        assertThrows(IllegalArgumentException.class, empty::to);
    }

    @Test
    void toSerializesKey() {
        byte[] keyBytes = new byte[] {4, 5, 6};
        OpenPgpKeyRecordDataDom dom = OpenPgpKeyRecordDataDom.builder().publicKey(keyBytes).build();

        assertArrayEquals(keyBytes, dom.to());
    }

    @Test
    void roundTripPreservesKey() {
        byte[] keyBytes = new byte[] {9, 8, 7, 6};
        OpenPgpKeyRecordDataDom original = OpenPgpKeyRecordDataDom.builder().publicKey(keyBytes).build();
        RDataDom decoded = OpenPgpKeyRecordDataDom.from(original.to());
        OpenPgpKeyRecordDataDom parsed = assertInstanceOf(OpenPgpKeyRecordDataDom.class, decoded);

        assertArrayEquals(keyBytes, parsed.publicKey());
    }
}
