package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpsRecordDataDomTest {

    @Test
    void fromRejectsShortInput() {
        assertThrows(IllegalArgumentException.class, () -> HttpsRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> HttpsRecordDataDom.from(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> HttpsRecordDataDom.from(new byte[1]));
        assertThrows(IllegalArgumentException.class, () -> HttpsRecordDataDom.from(new byte[2]));
    }

    @Test
    void fromParsesTargetNameAndParams() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        byte[] nameBytes = RDataUtils.toByteArray(name);
        byte[] priorityBytes = new byte[] {0, 1};
        byte[] paramKey3 = new byte[] {0, 3, 0, 0};
        byte[] paramKey5 = new byte[] {0, 5, 0, 3, 1, 2, 3};
        byte[] rdata = TestBytes.concat(priorityBytes, nameBytes, paramKey3, paramKey5);

        RDataDom dom = HttpsRecordDataDom.from(rdata);
        HttpsRecordDataDom https = assertInstanceOf(HttpsRecordDataDom.class, dom);

        assertEquals(1, https.svcPriority());
        assertEquals(List.of("svc", "example"), https.targetName().labels());
        assertEquals(2, https.svcParams().size());
        assertArrayEquals(new byte[0], https.svcParams().get(3));
        assertArrayEquals(new byte[] {1, 2, 3}, https.svcParams().get(5));
    }

    @Test
    void fromUsesResolverForCompressedName() {
        byte[] rdata = new byte[] {0, 5, (byte) 0xC0, 0x10};
        NameResolver resolver = offset -> DnsNameDom.builder().labels(List.of("svc", "example")).build();

        RDataDom dom = HttpsRecordDataDom.from(rdata, resolver);
        HttpsRecordDataDom https = assertInstanceOf(HttpsRecordDataDom.class, dom);

        assertEquals(5, https.svcPriority());
        assertEquals(List.of("svc", "example"), https.targetName().labels());
    }

    @Test
    void toRejectsNullTargetName() {
        HttpsRecordDataDom dom = HttpsRecordDataDom.builder().svcPriority(1).build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toRejectsInvalidPriority() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        HttpsRecordDataDom negative = HttpsRecordDataDom.builder().svcPriority(-1).targetName(name).build();
        HttpsRecordDataDom tooLarge = HttpsRecordDataDom.builder().svcPriority(0x1_0000).targetName(name).build();

        assertThrows(IllegalArgumentException.class, negative::to);
        assertThrows(IllegalArgumentException.class, tooLarge::to);
    }

    @Test
    void toRejectsNullParamValue() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        SortedMap<Integer, byte[]> params = new TreeMap<>();
        params.put(1, null);
        HttpsRecordDataDom dom = HttpsRecordDataDom.builder()
                .svcPriority(1)
                .targetName(name)
                .svcParams(params)
                .build();

        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toSerializesPriorityNameAndParams() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        SortedMap<Integer, byte[]> params = new TreeMap<>();
        params.put(3, new byte[0]);
        params.put(5, new byte[] {1, 2, 3});
        HttpsRecordDataDom dom = HttpsRecordDataDom.builder()
                .svcPriority(1)
                .targetName(name)
                .svcParams(params)
                .build();

        byte[] nameBytes = RDataUtils.toByteArray(name);
        byte[] priorityBytes = new byte[] {0, 1};
        byte[] paramKey3 = new byte[] {0, 3, 0, 0};
        byte[] paramKey5 = new byte[] {0, 5, 0, 3, 1, 2, 3};
        byte[] expected = TestBytes.concat(priorityBytes, nameBytes, paramKey3, paramKey5);

        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        SortedMap<Integer, byte[]> params = new TreeMap<>();
        params.put(1, new byte[0]);
        params.put(10, new byte[] {9, 8, 7});
        HttpsRecordDataDom original = HttpsRecordDataDom.builder()
                .svcPriority(12)
                .targetName(name)
                .svcParams(params)
                .build();

        RDataDom decoded = HttpsRecordDataDom.from(original.to());
        HttpsRecordDataDom parsed = assertInstanceOf(HttpsRecordDataDom.class, decoded);

        assertEquals(12, parsed.svcPriority());
        assertEquals(List.of("svc", "example"), parsed.targetName().labels());
        assertEquals(2, parsed.svcParams().size());
        assertArrayEquals(new byte[0], parsed.svcParams().get(1));
        assertArrayEquals(new byte[] {9, 8, 7}, parsed.svcParams().get(10));
    }

}
