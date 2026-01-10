// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DnsNameDomTest {
    private static final DnsNameDomPolicy HOSTNAME = DnsNameDomPolicy.Builtin.HOSTNAME;
    private static final DnsNameDomPolicy PROTOCOL = DnsNameDomPolicy.Builtin.PROTOCOL;

    @Test
    void endsWithReturnsTrueForExactMatch() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "www", "example", "com");
        DnsNameDom suffix = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "www", "example", "com");

        assertTrue(name.endsWith(suffix));
    }

    @Test
    void endsWithReturnsTrueForSuffixMatch() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "www", "example", "com");
        DnsNameDom suffix = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "example", "com");

        assertTrue(name.endsWith(suffix));
    }

    @Test
    void endsWithReturnsFalseWhenSuffixLongerThanName() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "example", "com");
        DnsNameDom suffix = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "www", "example", "com");

        assertFalse(name.endsWith(suffix));
    }

    @Test
    void endsWithReturnsFalseWhenLabelsDoNotMatch() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "www", "example", "com");
        DnsNameDom suffix = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "example", "org");

        assertFalse(name.endsWith(suffix));
    }

    @Test
    void endsWithReturnsTrueWhenSuffixIsRoot() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "www", "example", "com");
        DnsNameDom suffix = DnsNameDom.ROOT;

        assertTrue(name.endsWith(suffix));
    }

    @Test
    void endsWithIsCaseInsensitiveForHostnamePolicy() {
        DnsNameDom name = DnsNameDom.builder()
                .policy(HOSTNAME)
                .labelStrings("WWW", "Example", "Com")
                .build();
        DnsNameDom suffix = DnsNameDom.builder()
                .policy(HOSTNAME)
                .labelStrings("example", "com")
                .build();

        assertTrue(name.endsWith(suffix));
    }

    @Test
    void endsWithIsCaseSensitiveForProtocolPolicy() {
        DnsNameDom name = DnsNameDom.builder()
                .policy(PROTOCOL)
                .labelStrings("WWW", "Example", "Com")
                .build();
        DnsNameDom suffix = DnsNameDom.builder()
                .policy(PROTOCOL)
                .labelStrings("example", "com")
                .build();

        assertFalse(name.endsWith(suffix));
    }

    @Test
    void equalsAndHashCodeMatchForSameLabels() {
        DnsNameDom a = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "www", "example", "com");
        DnsNameDom b = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "www", "example", "com");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsAndHashCodeDifferForDifferentLabels() {
        DnsNameDom a = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "www", "example", "com");
        DnsNameDom b = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, "www", "example", "org");

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void caseInsentiveEqualsAndHashcodeForLabels() {
        DnsNameDom a = DnsNameDom.builder()
                .policy(HOSTNAME)
                .labelStrings("www", "example", "com")
                .build();
        DnsNameDom b = DnsNameDom.builder()
                .policy(HOSTNAME)
                .labelStrings("WWW", "EXAMPLE", "COM")
                .build();

        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }


    @Test
    void invalidCharactersInLabel() {
        // ascii order is '!' < '-' < '/'' < numbers < '=' < upper-case < '^' < lower-case < '~' 
        // this check for any position in that ordering
        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(
                DnsNameDom.builder().policy(HOSTNAME).labelStrings("example", "c!om").build()));
        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(
                DnsNameDom.builder().policy(HOSTNAME).labelStrings("example", "c/om").build()));
        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(
                DnsNameDom.builder().policy(HOSTNAME).labelStrings("example", "c=om").build()));
        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(
                DnsNameDom.builder().policy(HOSTNAME).labelStrings("example", "c^om").build()));
        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(
                DnsNameDom.builder().policy(HOSTNAME).labelStrings("example", "c~om").build()));
    }

    @Test
    void invalidHyphenPositionLabel() {
        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(
                DnsNameDom.builder().policy(HOSTNAME).labelStrings("-example", "com").build()));
        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(
                DnsNameDom.builder().policy(HOSTNAME).labelStrings("example-", "com").build()));
        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(
                DnsNameDom.builder().policy(HOSTNAME).labelStrings("example", "-com").build()));
        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(
                DnsNameDom.builder().policy(HOSTNAME).labelStrings("example", "com-").build()));
    }

    @Test
    void validHostnameLabelsAreAccepted() {
        DnsNameDom hostname = DnsNameDom.builder()
                .policy(HOSTNAME)
                .labelStrings("www", "example", "com")
                .build();
        DnsNameDom hostname2 = DnsNameDom.builder()
                .policy(HOSTNAME)
                .labelStrings("example", "com")
                .build();

        HOSTNAME.validateOrThrow(hostname);
        HOSTNAME.validateOrThrow(hostname2);
    }


}
