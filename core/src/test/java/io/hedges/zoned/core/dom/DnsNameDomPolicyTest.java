// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsNameDomPolicyTest {

    private static final DnsNameDomPolicy HOSTNAME = DnsNameDomPolicy.Builtin.HOSTNAME;
    private static final DnsNameDomPolicy PROTOCOL = DnsNameDomPolicy.Builtin.PROTOCOL;

    @Test
    void equalNamesIsCaseInsensitiveForHostnames() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("WWW", "EXAMPLE", "COM");

        assertTrue(HOSTNAME.equalNames(a, b));
        assertTrue(HOSTNAME.equalNames(b, a));
    }

    @Test
    void hashFlatLabelsReturnDiffer() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("www", "examplecom");

        assertNotEquals(HOSTNAME.hashName(a), HOSTNAME.hashName(b));
    }

    @Test
    void equalFlatLabelsArentEqual() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("www", "examplecom");

        assertFalse(HOSTNAME.equalNames(a, b));
        assertFalse(HOSTNAME.equalNames(b, a));
    }

    @Test
    void equalNamesReturnsFalseWhenLabelCountDiffers() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("example", "com");

        assertFalse(HOSTNAME.equalNames(a, b));
    }

    @Test
    void equalNamesReturnsFalseWhenLabelsDiffer() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("www", "example", "org");

        assertFalse(HOSTNAME.equalNames(a, b));
    }

    @Test
    void hashNameMatchesForCaseInsensitiveEquals() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("WWW", "EXAMPLE", "COM");

        assertEquals(HOSTNAME.hashName(a), HOSTNAME.hashName(b));
    }

    @Test
    void validateOrThrowRejectsEmptyHostnameLabel() {
        DnsNameDom name = DnsNameDom.labels("www", "", "com");

        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(name));
    }

    @Test
    void validateOrThrowRejectsLeadingHyphenHostnameLabel() {
        DnsNameDom name = DnsNameDom.labels("-example", "com");

        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(name));
    }

    @Test
    void validateOrThrowRejectsTrailingHyphenHostnameLabel() {
        DnsNameDom name = DnsNameDom.labels("example-", "com");

        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(name));
    }

    @Test
    void validateOrThrowRejectsInvalidHostnameCharacters() {
        DnsNameDom name = DnsNameDom.labels("exa_mple", "com");

        assertThrows(IllegalArgumentException.class, () -> HOSTNAME.validateOrThrow(name));
    }

    @Test
    void validateOrThrowAcceptsValidHostnameLabels() {
        DnsNameDom name = DnsNameDom.labels("www", "exa-mple", "com");

        assertDoesNotThrow(() -> HOSTNAME.validateOrThrow(name));
    }

    @Test
    void protocolEqualNamesIsCaseSensitive() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("WWW", "example", "com");

        assertFalse(PROTOCOL.equalNames(a, b));
    }

    @Test
    void protocolHashNameDiffersForDifferentCase() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("WWW", "example", "com");

        assertNotEquals(PROTOCOL.hashName(a), PROTOCOL.hashName(b));
    }

    @Test
    void protocolEqualNamesReturnsFalseWhenLabelsDiffer() {
        DnsNameDom a = DnsNameDom.labels("www", "example", "com");
        DnsNameDom b = DnsNameDom.labels("www", "example", "org");

        assertFalse(PROTOCOL.equalNames(a, b));
    }

    @Test
    void protocolValidateOrThrowRejectsEmptyLabel() {
        DnsNameDom name = DnsNameDom.labels("www", "", "com");

        assertThrows(IllegalArgumentException.class, () -> PROTOCOL.validateOrThrow(name));
    }

    @Test
    void protocolValidateOrThrowAcceptsRoot() {
        DnsNameDom name = DnsNameDom.ROOT;

        assertDoesNotThrow(() -> PROTOCOL.validateOrThrow(name));
    }
}
