// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsyncRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> CsyncRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> CsyncRecordDataDom.from(new byte[5]));
        assertThrows(IllegalArgumentException.class, () -> CsyncRecordDataDom.from(new byte[6]));
    }

    @Test
    void fromRejectsZeroLengthWindow() {
        byte[] rdata = new byte[] {0, 0, 0, 1, 0, 0, 0, 0};
        assertThrows(IllegalArgumentException.class, () -> CsyncRecordDataDom.from(rdata));
    }

    @Test
    void fromRejectsTruncatedWindowHeader() {
        byte[] rdata = new byte[] {0, 0, 0, 1, 0, 0, 0};
        assertThrows(IllegalArgumentException.class, () -> CsyncRecordDataDom.from(rdata));
    }

    @Test
    void fromRejectsEmptyTypeBitmap() {
        byte[] rdata = new byte[] {
                0, 0, 0, 1,
                0, 0,
                0, 1,
                0x00
        };
        assertThrows(IllegalArgumentException.class, () -> CsyncRecordDataDom.from(rdata));
    }

    @Test
    void fromRejectsNonIncreasingWindows() {
        byte[] rdata = new byte[] {
                0, 0, 0, 1,
                0, 0,
                1, 1,
                0x40,
                0, 1,
                0x40
        };
        assertThrows(IllegalArgumentException.class, () -> CsyncRecordDataDom.from(rdata));
    }

    @Test
    void fromParsesFields() {
        byte[] rdata = new byte[] {
                0, 0, 0, 1,
                0, 0,
                0, 4,
                0x60, 0x00, 0x00, 0x08
        };

        RDataDom dom = CsyncRecordDataDom.from(rdata);
        CsyncRecordDataDom csync = assertInstanceOf(CsyncRecordDataDom.class, dom);

        assertEquals(1L, csync.serial());
        assertEquals(0, csync.flags());
        assertEquals(List.of(DnsRecordTypeDom.A, DnsRecordTypeDom.NS, DnsRecordTypeDom.AAAA), csync.types());
    }

    @Test
    void toRejectsInvalidFields() {
        CsyncRecordDataDom missingTypes = CsyncRecordDataDom.builder()
                .serial(1)
                .flags(0)
                .build();
        CsyncRecordDataDom emptyTypes = CsyncRecordDataDom.builder()
                .serial(1)
                .flags(0)
                .types(List.of())
                .build();
        CsyncRecordDataDom negativeSerial = CsyncRecordDataDom.builder()
                .serial(-1)
                .flags(0)
                .types(List.of(DnsRecordTypeDom.A))
                .build();
        CsyncRecordDataDom tooLargeSerial = CsyncRecordDataDom.builder()
                .serial(0x1_0000_0000L)
                .flags(0)
                .types(List.of(DnsRecordTypeDom.A))
                .build();
        CsyncRecordDataDom badFlags = CsyncRecordDataDom.builder()
                .serial(1)
                .flags(0x1_0000)
                .types(List.of(DnsRecordTypeDom.A))
                .build();
        CsyncRecordDataDom nullType = CsyncRecordDataDom.builder()
                .serial(1)
                .flags(0)
                .types(java.util.Arrays.asList((DnsRecordTypeDom) null))
                .build();
        CsyncRecordDataDom duplicateTypes = CsyncRecordDataDom.builder()
                .serial(1)
                .flags(0)
                .types(List.of(DnsRecordTypeDom.A, DnsRecordTypeDom.A))
                .build();

        assertThrows(IllegalArgumentException.class, missingTypes::to);
        assertThrows(IllegalArgumentException.class, emptyTypes::to);
        assertThrows(IllegalArgumentException.class, negativeSerial::to);
        assertThrows(IllegalArgumentException.class, tooLargeSerial::to);
        assertThrows(IllegalArgumentException.class, badFlags::to);
        assertThrows(IllegalArgumentException.class, nullType::to);
        assertThrows(IllegalArgumentException.class, duplicateTypes::to);
    }

    @Test
    void toSerializesRdata() {
        CsyncRecordDataDom dom = CsyncRecordDataDom.builder()
                .serial(1)
                .flags(0)
                .types(List.of(DnsRecordTypeDom.AAAA, DnsRecordTypeDom.NS, DnsRecordTypeDom.A))
                .build();

        byte[] expected = new byte[] {
                0, 0, 0, 1,
                0, 0,
                0, 4,
                0x60, 0x00, 0x00, 0x08
        };
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        CsyncRecordDataDom original = CsyncRecordDataDom.builder()
                .serial(42)
                .flags(1)
                .types(List.of(DnsRecordTypeDom.NS, DnsRecordTypeDom.A))
                .build();

        RDataDom decoded = CsyncRecordDataDom.from(original.to());
        CsyncRecordDataDom parsed = assertInstanceOf(CsyncRecordDataDom.class, decoded);

        assertEquals(42, parsed.serial());
        assertEquals(1, parsed.flags());
        assertEquals(List.of(DnsRecordTypeDom.A, DnsRecordTypeDom.NS), parsed.types());
    }
}
