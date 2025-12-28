package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaaRecordDataDomTest {

    @Test
    void fromRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> CaaRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> CaaRecordDataDom.from(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> CaaRecordDataDom.from(new byte[] {0}));
        assertThrows(IllegalArgumentException.class, () -> CaaRecordDataDom.from(new byte[] {0, 0}));
        assertThrows(IllegalArgumentException.class, () -> CaaRecordDataDom.from(new byte[] {0, 2, 'i'}));
        assertThrows(IllegalArgumentException.class, () -> CaaRecordDataDom.from(new byte[] {0, 1, '!'}));
        assertThrows(IllegalArgumentException.class, () -> CaaRecordDataDom.from(new byte[] {0, 16, 't'}));
    }

    @Test
    void fromParsesFields() {
        byte[] value = "letsencrypt.org".getBytes(StandardCharsets.US_ASCII);
        byte[] rdata = TestBytes.concat(
                new byte[] {(byte) 0x80, 5, 'i', 's', 's', 'u', 'e'},
                value
        );

        RDataDom dom = CaaRecordDataDom.from(rdata);
        CaaRecordDataDom caa = assertInstanceOf(CaaRecordDataDom.class, dom);

        assertEquals(0x80, caa.flags());
        assertEquals("issue", caa.tag());
        assertArrayEquals(value, caa.value());
    }

    @Test
    void toRejectsInvalidFields() {
        CaaRecordDataDom missingTag = CaaRecordDataDom.builder().flags(0).value(new byte[0]).build();
        CaaRecordDataDom emptyTag = CaaRecordDataDom.builder().flags(0).tag("").value(new byte[0]).build();
        CaaRecordDataDom invalidTag = CaaRecordDataDom.builder().flags(0).tag("iss_ue").value(new byte[0]).build();
        CaaRecordDataDom tooLong = CaaRecordDataDom.builder().flags(0).tag("toolongtagvalue1").value(new byte[0]).build();
        CaaRecordDataDom missingValue = CaaRecordDataDom.builder().flags(0).tag("issue").build();
        CaaRecordDataDom negativeFlags = CaaRecordDataDom.builder().flags(-1).tag("issue").value(new byte[0]).build();
        CaaRecordDataDom tooLargeFlags = CaaRecordDataDom.builder().flags(256).tag("issue").value(new byte[0]).build();

        assertThrows(IllegalArgumentException.class, missingTag::to);
        assertThrows(IllegalArgumentException.class, emptyTag::to);
        assertThrows(IllegalArgumentException.class, invalidTag::to);
        assertThrows(IllegalArgumentException.class, tooLong::to);
        assertThrows(IllegalArgumentException.class, missingValue::to);
        assertThrows(IllegalArgumentException.class, negativeFlags::to);
        assertThrows(IllegalArgumentException.class, tooLargeFlags::to);
    }

    @Test
    void toSerializesFields() {
        byte[] value = "letsencrypt.org".getBytes(StandardCharsets.US_ASCII);
        CaaRecordDataDom dom = CaaRecordDataDom.builder()
                .flags(0)
                .tag("issue")
                .value(value)
                .build();

        byte[] encoded = dom.to();

        assertArrayEquals(
                TestBytes.concat(
                        new byte[] {0, 5, 'i', 's', 's', 'u', 'e'},
                        value
                ),
                encoded
        );
    }

    @Test
    void roundTripPreservesFields() {
        byte[] value = "letsencrypt.org".getBytes(StandardCharsets.US_ASCII);
        CaaRecordDataDom original = CaaRecordDataDom.builder()
                .flags(0x80)
                .tag("issue")
                .value(value)
                .build();

        RDataDom decoded = CaaRecordDataDom.from(original.to());
        CaaRecordDataDom parsed = assertInstanceOf(CaaRecordDataDom.class, decoded);

        assertEquals(0x80, parsed.flags());
        assertEquals("issue", parsed.tag());
        assertArrayEquals(value, parsed.value());
    }
}
