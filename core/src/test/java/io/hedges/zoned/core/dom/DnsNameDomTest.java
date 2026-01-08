// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DnsNameDomTest {

    @Test
    void endsWithReturnsTrueForExactMatch() {
        DnsNameDom name = DnsNameDom.labels("www", "example", "com");
        DnsNameDom suffix = DnsNameDom.labels("www", "example", "com");

        assertTrue(name.endsWith(suffix));
    }

    @Test
    void endsWithReturnsTrueForSuffixMatch() {
        DnsNameDom name = DnsNameDom.labels("www", "example", "com");
        DnsNameDom suffix = DnsNameDom.labels("example", "com");

        assertTrue(name.endsWith(suffix));
    }

    @Test
    void endsWithReturnsFalseWhenSuffixLongerThanName() {
        DnsNameDom name = DnsNameDom.labels("example", "com");
        DnsNameDom suffix = DnsNameDom.labels("www", "example", "com");

        assertFalse(name.endsWith(suffix));
    }

    @Test
    void endsWithReturnsFalseWhenLabelsDoNotMatch() {
        DnsNameDom name = DnsNameDom.labels("www", "example", "com");
        DnsNameDom suffix = DnsNameDom.labels("example", "org");

        assertFalse(name.endsWith(suffix));
    }

    @Test
    void endsWithReturnsTrueWhenSuffixIsRoot() {
        DnsNameDom name = DnsNameDom.labels("www", "example", "com");
        DnsNameDom suffix = DnsNameDom.ROOT;

        assertTrue(name.endsWith(suffix));
    }

    @Test
    void equalsAndHashCodeMatchForSameLabels() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("www", "example", "com");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsAndHashCodeDifferForDifferentLabels() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("www", "example", "org");

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }


}
