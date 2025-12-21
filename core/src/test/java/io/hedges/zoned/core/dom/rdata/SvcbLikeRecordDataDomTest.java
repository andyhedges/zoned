package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SvcbLikeRecordDataDomTest {

    private static Stream<Arguments> specs() {
        return Stream.of(
                Arguments.of(new Spec(
                        (rdata, resolver) -> HttpsRecordDataDom.from(rdata, resolver),
                        HttpsRecordDataDom::from,
                        (priority, name, params) -> HttpsRecordDataDom.builder()
                                .svcPriority(priority)
                                .targetName(name)
                                .svcParams(params)
                                .build(),
                        new Accessor() {
                            @Override
                            public int svcPriority(RDataDom dom) {
                                return assertInstanceOf(HttpsRecordDataDom.class, dom).svcPriority();
                            }

                            @Override
                            public DnsNameDom targetName(RDataDom dom) {
                                return assertInstanceOf(HttpsRecordDataDom.class, dom).targetName();
                            }

                            @Override
                            public SortedMap<Integer, byte[]> svcParams(RDataDom dom) {
                                return assertInstanceOf(HttpsRecordDataDom.class, dom).svcParams();
                            }
                        }
                )),
                Arguments.of(new Spec(
                        (rdata, resolver) -> SvcbRecordDataDom.from(rdata, resolver),
                        SvcbRecordDataDom::from,
                        (priority, name, params) -> SvcbRecordDataDom.builder()
                                .svcPriority(priority)
                                .targetName(name)
                                .svcParams(params)
                                .build(),
                        new Accessor() {
                            @Override
                            public int svcPriority(RDataDom dom) {
                                return assertInstanceOf(SvcbRecordDataDom.class, dom).svcPriority();
                            }

                            @Override
                            public DnsNameDom targetName(RDataDom dom) {
                                return assertInstanceOf(SvcbRecordDataDom.class, dom).targetName();
                            }

                            @Override
                            public SortedMap<Integer, byte[]> svcParams(RDataDom dom) {
                                return assertInstanceOf(SvcbRecordDataDom.class, dom).svcParams();
                            }
                        }
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("specs")
    void fromRejectsShortInput(Spec spec) {
        assertThrows(IllegalArgumentException.class, () -> spec.from.apply(null));
        assertThrows(IllegalArgumentException.class, () -> spec.from.apply(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> spec.from.apply(new byte[1]));
        assertThrows(IllegalArgumentException.class, () -> spec.from.apply(new byte[2]));
    }

    @ParameterizedTest
    @MethodSource("specs")
    void fromParsesTargetNameAndParams(Spec spec) {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        byte[] nameBytes = RDataUtils.toByteArray(name);
        byte[] priorityBytes = new byte[] {0, 1};
        byte[] paramKey3 = new byte[] {0, 3, 0, 0};
        byte[] paramKey5 = new byte[] {0, 5, 0, 3, 1, 2, 3};
        byte[] rdata = TestBytes.concat(priorityBytes, nameBytes, paramKey3, paramKey5);

        RDataDom dom = spec.from.apply(rdata);

        assertEquals(1, spec.accessor.svcPriority(dom));
        assertEquals(List.of("svc", "example"), spec.accessor.targetName(dom).labels());
        assertEquals(2, spec.accessor.svcParams(dom).size());
        assertArrayEquals(new byte[0], spec.accessor.svcParams(dom).get(3));
        assertArrayEquals(new byte[] {1, 2, 3}, spec.accessor.svcParams(dom).get(5));
    }

    @ParameterizedTest
    @MethodSource("specs")
    void fromUsesResolverForCompressedName(Spec spec) {
        byte[] rdata = new byte[] {0, 5, (byte) 0xC0, 0x10};
        NameResolver resolver = offset -> DnsNameDom.builder().labels(List.of("svc", "example")).build();

        RDataDom dom = spec.fromWithResolver.apply(rdata, resolver);

        assertEquals(5, spec.accessor.svcPriority(dom));
        assertEquals(List.of("svc", "example"), spec.accessor.targetName(dom).labels());
    }

    @ParameterizedTest
    @MethodSource("specs")
    void toRejectsNullTargetName(Spec spec) {
        RDataDom dom = spec.builder.build(1, null, null);
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @ParameterizedTest
    @MethodSource("specs")
    void toRejectsInvalidPriority(Spec spec) {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        RDataDom negative = spec.builder.build(-1, name, null);
        RDataDom tooLarge = spec.builder.build(0x1_0000, name, null);

        assertThrows(IllegalArgumentException.class, negative::to);
        assertThrows(IllegalArgumentException.class, tooLarge::to);
    }

    @ParameterizedTest
    @MethodSource("specs")
    void toRejectsNullParamValue(Spec spec) {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        SortedMap<Integer, byte[]> params = new TreeMap<>();
        params.put(1, null);
        RDataDom dom = spec.builder.build(1, name, params);

        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @ParameterizedTest
    @MethodSource("specs")
    void toSerializesPriorityNameAndParams(Spec spec) {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        SortedMap<Integer, byte[]> params = new TreeMap<>();
        params.put(3, new byte[0]);
        params.put(5, new byte[] {1, 2, 3});
        RDataDom dom = spec.builder.build(1, name, params);

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
    @MethodSource("specs")
    void roundTripPreservesFields(Spec spec) {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("svc", "example")).build();
        SortedMap<Integer, byte[]> params = new TreeMap<>();
        params.put(1, new byte[0]);
        params.put(10, new byte[] {9, 8, 7});
        RDataDom original = spec.builder.build(12, name, params);

        RDataDom decoded = spec.from.apply(original.to());

        assertEquals(12, spec.accessor.svcPriority(decoded));
        assertEquals(List.of("svc", "example"), spec.accessor.targetName(decoded).labels());
        assertEquals(2, spec.accessor.svcParams(decoded).size());
        assertArrayEquals(new byte[0], spec.accessor.svcParams(decoded).get(1));
        assertArrayEquals(new byte[] {9, 8, 7}, spec.accessor.svcParams(decoded).get(10));
    }

    private interface Builder {
        RDataDom build(int svcPriority, DnsNameDom targetName, SortedMap<Integer, byte[]> svcParams);
    }

    private interface Accessor {
        int svcPriority(RDataDom dom);

        DnsNameDom targetName(RDataDom dom);

        SortedMap<Integer, byte[]> svcParams(RDataDom dom);
    }

    private record Spec(
            BiFunction<byte[], NameResolver, RDataDom> fromWithResolver,
            Function<byte[], RDataDom> from,
            Builder builder,
            Accessor accessor) {
    }
}
