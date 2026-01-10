// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.DnsNameDomPolicy;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HipRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("rvs", "example"));

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> HipRecordDataDom.from(null, null));
        assertThrows(IllegalArgumentException.class, () -> HipRecordDataDom.from(new byte[4], null));
    }

    @Test
    void fromRejectsInvalidHitAndKeyLengths() {
        byte[] zeroHit = new byte[] {0, 1, 0, 1, 0};
        byte[] zeroKey = new byte[] {1, 1, 0, 0, 0};
        byte[] tooShort = new byte[] {1, 1, 0, 2, 1};
        assertThrows(IllegalArgumentException.class, () -> HipRecordDataDom.from(zeroHit, null));
        assertThrows(IllegalArgumentException.class, () -> HipRecordDataDom.from(zeroKey, null));
        assertThrows(IllegalArgumentException.class, () -> HipRecordDataDom.from(tooShort, null));
    }

    @Test
    void fromParsesFieldsWithoutRendezvousServers() {
        byte[] rdata = new byte[] {4, 1, 0, 3, 1, 2, 3, 4, 9, 8, 7};
        RDataDom dom = HipRecordDataDom.from(rdata, null);
        HipRecordDataDom hip = assertInstanceOf(HipRecordDataDom.class, dom);

        assertArrayEquals(new byte[] {1, 2, 3, 4}, hip.hit());
        assertEquals(1, hip.algorithm());
        assertArrayEquals(new byte[] {9, 8, 7}, hip.publicKey());
        assertEquals(List.of(), hip.rendezvousServers());
    }

    @Test
    void fromParsesRendezvousServers() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("rvs", "example", "test"));
        byte[] nameBytes = RDataUtils.toByteArray(name);
        byte[] rdata = new byte[4 + 4 + 3 + nameBytes.length];
        int idx = 0;
        rdata[idx++] = 4;
        rdata[idx++] = 2;
        rdata[idx++] = 0;
        rdata[idx++] = 3;
        System.arraycopy(new byte[] {1, 2, 3, 4}, 0, rdata, idx, 4);
        idx += 4;
        System.arraycopy(new byte[] {5, 6, 7}, 0, rdata, idx, 3);
        idx += 3;
        System.arraycopy(nameBytes, 0, rdata, idx, nameBytes.length);

        RDataDom dom = HipRecordDataDom.from(rdata, null);
        HipRecordDataDom hip = assertInstanceOf(HipRecordDataDom.class, dom);

        assertEquals(List.of("rvs", "example", "test"), hip.rendezvousServers().get(0).labelStrings());
    }

    @Test
    void fromUsesResolverForCompressedNames() {
        byte[] rdata = new byte[] {4, 1, 0, 3, 1, 2, 3, 4, 9, 8, 7, (byte) 0xC0, 0x10};
        RDataDom dom = HipRecordDataDom.from(rdata, RESOLVER);
        HipRecordDataDom hip = assertInstanceOf(HipRecordDataDom.class, dom);

        assertEquals(List.of("rvs", "example"), hip.rendezvousServers().get(0).labelStrings());
    }

    @Test
    void toRejectsInvalidFields() {
        HipRecordDataDom missingHit = HipRecordDataDom.builder()
                .algorithm(1)
                .publicKey(new byte[] {1})
                .rendezvousServers(List.of())
                .build();
        HipRecordDataDom emptyHit = HipRecordDataDom.builder()
                .hit(new byte[0])
                .algorithm(1)
                .publicKey(new byte[] {1})
                .rendezvousServers(List.of())
                .build();
        HipRecordDataDom tooLongHit = HipRecordDataDom.builder()
                .hit(new byte[256])
                .algorithm(1)
                .publicKey(new byte[] {1})
                .rendezvousServers(List.of())
                .build();
        HipRecordDataDom missingKey = HipRecordDataDom.builder()
                .hit(new byte[] {1})
                .algorithm(1)
                .rendezvousServers(List.of())
                .build();
        HipRecordDataDom emptyKey = HipRecordDataDom.builder()
                .hit(new byte[] {1})
                .algorithm(1)
                .publicKey(new byte[0])
                .rendezvousServers(List.of())
                .build();
        HipRecordDataDom tooLongKey = HipRecordDataDom.builder()
                .hit(new byte[] {1})
                .algorithm(1)
                .publicKey(new byte[0x1_0000])
                .rendezvousServers(List.of())
                .build();
        HipRecordDataDom badAlgorithm = HipRecordDataDom.builder()
                .hit(new byte[] {1})
                .algorithm(256)
                .publicKey(new byte[] {1})
                .rendezvousServers(List.of())
                .build();
        HipRecordDataDom negativeAlgorithm = HipRecordDataDom.builder()
                .hit(new byte[] {1})
                .algorithm(-1)
                .publicKey(new byte[] {1})
                .rendezvousServers(List.of())
                .build();
        HipRecordDataDom nullRvList = HipRecordDataDom.builder()
                .hit(new byte[] {1})
                .algorithm(1)
                .publicKey(new byte[] {1})
                .build();
        HipRecordDataDom nullRvEntry = HipRecordDataDom.builder()
                .hit(new byte[] {1})
                .algorithm(1)
                .publicKey(new byte[] {1})
                .rendezvousServers(java.util.Arrays.asList((DnsNameDom) null))
                .build();

        assertThrows(IllegalArgumentException.class, missingHit::to);
        assertThrows(IllegalArgumentException.class, emptyHit::to);
        assertThrows(IllegalArgumentException.class, tooLongHit::to);
        assertThrows(IllegalArgumentException.class, missingKey::to);
        assertThrows(IllegalArgumentException.class, emptyKey::to);
        assertThrows(IllegalArgumentException.class, tooLongKey::to);
        assertThrows(IllegalArgumentException.class, badAlgorithm::to);
        assertThrows(IllegalArgumentException.class, negativeAlgorithm::to);
        assertThrows(IllegalArgumentException.class, nullRvList::to);
        assertThrows(IllegalArgumentException.class, nullRvEntry::to);
    }

    @Test
    void toSerializesRdata() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("rvs", "example", "test"));
        HipRecordDataDom dom = HipRecordDataDom.builder()
                .hit(new byte[] {1, 2, 3, 4})
                .algorithm(2)
                .publicKey(new byte[] {5, 6, 7})
                .rendezvousServers(List.of(name))
                .build();

        byte[] expectedName = RDataUtils.toByteArray(name);
        byte[] expected = new byte[4 + 4 + 3 + expectedName.length];
        int idx = 0;
        expected[idx++] = 4;
        expected[idx++] = 2;
        expected[idx++] = 0;
        expected[idx++] = 3;
        System.arraycopy(new byte[] {1, 2, 3, 4}, 0, expected, idx, 4);
        idx += 4;
        System.arraycopy(new byte[] {5, 6, 7}, 0, expected, idx, 3);
        idx += 3;
        System.arraycopy(expectedName, 0, expected, idx, expectedName.length);

        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("rvs", "example", "test"));
        HipRecordDataDom original = HipRecordDataDom.builder()
                .hit(new byte[] {10, 11, 12, 13})
                .algorithm(1)
                .publicKey(new byte[] {4, 5, 6})
                .rendezvousServers(List.of(name))
                .build();

        RDataDom decoded = HipRecordDataDom.from(original.to(), null);
        HipRecordDataDom parsed = assertInstanceOf(HipRecordDataDom.class, decoded);

        assertArrayEquals(new byte[] {10, 11, 12, 13}, parsed.hit());
        assertEquals(1, parsed.algorithm());
        assertArrayEquals(new byte[] {4, 5, 6}, parsed.publicKey());
        assertEquals(List.of("rvs", "example", "test"), parsed.rendezvousServers().get(0).labelStrings());
    }
}