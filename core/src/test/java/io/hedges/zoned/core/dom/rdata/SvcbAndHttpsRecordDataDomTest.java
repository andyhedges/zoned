// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SvcbAndHttpsRecordDataDomTest {

    private static Stream<RecordType> recordTypes() {
        return Stream.of(RecordType.HTTPS, RecordType.SVCB);
    }

    @ParameterizedTest
    @MethodSource("recordTypes")
    void fromRejectsShortInput(RecordType type) {
        assertThrows(IllegalArgumentException.class, () -> type.from(null, null));
        assertThrows(IllegalArgumentException.class, () -> type.from(new byte[0], null));
        assertThrows(IllegalArgumentException.class, () -> type.from(new byte[1], null));
        assertThrows(IllegalArgumentException.class, () -> type.from(new byte[2], null));
    }

    @ParameterizedTest
    @MethodSource("recordTypes")
    void fromParsesTargetNameAndParams(RecordType type) {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        byte[] nameBytes = RDataUtils.toByteArray(name);
        byte[] priorityBytes = new byte[] {0, 1};
        byte[] paramKey3 = new byte[] {0, 3, 0, 0};
        byte[] paramKey5 = new byte[] {0, 5, 0, 3, 1, 2, 3};
        byte[] rdata = TestBytes.concat(priorityBytes, nameBytes, paramKey3, paramKey5);

        RDataDom dom = type.from(rdata, null);

        assertEquals(1, type.svcPriority(dom));
        assertEquals(List.of("svc", "example"), type.targetName(dom).labels());
        assertEquals(2, type.svcParams(dom).size());
        assertArrayEquals(new byte[0], type.svcParams(dom).get(3));
        assertArrayEquals(new byte[] {1, 2, 3}, type.svcParams(dom).get(5));
    }

    @ParameterizedTest
    @MethodSource("recordTypes")
    void fromUsesResolverForCompressedName(RecordType type) {
        byte[] rdata = new byte[] {0, 5, (byte) 0xC0, 0x10};
        NameResolver resolver = offset -> DnsNameDom.builder().labels(List.of("svc", "example")).build();

        RDataDom dom = type.from(rdata, resolver);

        assertEquals(5, type.svcPriority(dom));
        assertEquals(List.of("svc", "example"), type.targetName(dom).labels());
    }

    @ParameterizedTest
    @MethodSource("recordTypes")
    void toRejectsNullTargetName(RecordType type) {
        RDataDom dom = type.build(1, null, null);
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @ParameterizedTest
    @MethodSource("recordTypes")
    void toRejectsInvalidPriority(RecordType type) {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        RDataDom negative = type.build(-1, name, null);
        RDataDom tooLarge = type.build(0x1_0000, name, null);

        assertThrows(IllegalArgumentException.class, negative::to);
        assertThrows(IllegalArgumentException.class, tooLarge::to);
    }

    @ParameterizedTest
    @MethodSource("recordTypes")
    void toRejectsNullParamValue(RecordType type) {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        SortedMap<Integer, byte[]> params = new TreeMap<>();
        params.put(1, null);
        RDataDom dom = type.build(1, name, params);

        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @ParameterizedTest
    @MethodSource("recordTypes")
    void toSerializesPriorityNameAndParams(RecordType type) {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        SortedMap<Integer, byte[]> params = new TreeMap<>();
        params.put(3, new byte[0]);
        params.put(5, new byte[] {1, 2, 3});
        RDataDom dom = type.build(1, name, params);

        byte[] nameBytes = RDataUtils.toByteArray(name);
        byte[] expected = TestBytes.concat(
                new byte[] {0, 1},
                nameBytes,
                new byte[] {0, 3, 0, 0},
                new byte[] {0, 5, 0, 3, 1, 2, 3}
        );

        assertArrayEquals(expected, dom.to());
    }

    @ParameterizedTest
    @MethodSource("recordTypes")
    void roundTripPreservesFields(RecordType type) {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        SortedMap<Integer, byte[]> params = new TreeMap<>();
        params.put(1, new byte[0]);
        params.put(10, new byte[] {9, 8, 7});
        RDataDom original = type.build(12, name, params);

        RDataDom decoded = type.from(original.to(), null);

        assertEquals(12, type.svcPriority(decoded));
        assertEquals(List.of("svc", "example"), type.targetName(decoded).labels());
        assertEquals(2, type.svcParams(decoded).size());
        assertArrayEquals(new byte[0], type.svcParams(decoded).get(1));
        assertArrayEquals(new byte[] {9, 8, 7}, type.svcParams(decoded).get(10));
    }

    private enum RecordType {
        HTTPS {
            @Override
            RDataDom from(byte[] rdata, NameResolver resolver) {
                return HttpsRecordDataDom.from(rdata, resolver);
            }

            @Override
            RDataDom build(int svcPriority, DnsNameDom targetName, SortedMap<Integer, byte[]> svcParams) {
                return HttpsRecordDataDom.builder()
                        .svcPriority(svcPriority)
                        .targetName(targetName)
                        .svcParams(svcParams)
                        .build();
            }

            @Override
            int svcPriority(RDataDom dom) {
                return assertInstanceOf(HttpsRecordDataDom.class, dom).svcPriority();
            }

            @Override
            DnsNameDom targetName(RDataDom dom) {
                return assertInstanceOf(HttpsRecordDataDom.class, dom).targetName();
            }

            @Override
            SortedMap<Integer, byte[]> svcParams(RDataDom dom) {
                return assertInstanceOf(HttpsRecordDataDom.class, dom).svcParams();
            }
        },
        SVCB {
            @Override
            RDataDom from(byte[] rdata, NameResolver resolver) {
                return SvcbRecordDataDom.from(rdata, resolver);
            }

            @Override
            RDataDom build(int svcPriority, DnsNameDom targetName, SortedMap<Integer, byte[]> svcParams) {
                return SvcbRecordDataDom.builder()
                        .svcPriority(svcPriority)
                        .targetName(targetName)
                        .svcParams(svcParams)
                        .build();
            }

            @Override
            int svcPriority(RDataDom dom) {
                return assertInstanceOf(SvcbRecordDataDom.class, dom).svcPriority();
            }

            @Override
            DnsNameDom targetName(RDataDom dom) {
                return assertInstanceOf(SvcbRecordDataDom.class, dom).targetName();
            }

            @Override
            SortedMap<Integer, byte[]> svcParams(RDataDom dom) {
                return assertInstanceOf(SvcbRecordDataDom.class, dom).svcParams();
            }
        };

        abstract RDataDom from(byte[] rdata, NameResolver resolver);

        abstract RDataDom build(int svcPriority, DnsNameDom targetName, SortedMap<Integer, byte[]> svcParams);

        abstract int svcPriority(RDataDom dom);

        abstract DnsNameDom targetName(RDataDom dom);

        abstract SortedMap<Integer, byte[]> svcParams(RDataDom dom);
    }
}
