// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.resolver;

import io.hedges.zoned.core.dom.DnsNameDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolveUtilsTest {

    @Test
    void returnsTrueWhenLabelsMatchExactly() {
        DnsNameDom authority = DnsNameDom.labels("example", "com");
        DnsNameDom domain = DnsNameDom.labels("example", "com");

        assertTrue(ResolveUtils.isInBailiwick(authority, domain));
    }

    @Test
    void returnsFalseWhenAuthorityHasMoreLabelsThanDomain() {
        DnsNameDom authority = DnsNameDom.labels("example", "com", "extra");
        DnsNameDom domain = DnsNameDom.labels("example", "com");

        assertFalse(ResolveUtils.isInBailiwick(authority, domain));
    }

    @Test
    void returnsFalseWhenLabelsDiffer() {
        DnsNameDom authority = DnsNameDom.labels("example", "org");
        DnsNameDom domain = DnsNameDom.labels("example", "com");

        assertFalse(ResolveUtils.isInBailiwick(authority, domain));
    }

    @Test
    void throwsWhenAuthorityIsShorterButPrefixMatches() {
        DnsNameDom authority = DnsNameDom.labels("example");
        DnsNameDom domain = DnsNameDom.labels("example", "com");

        assertFalse(ResolveUtils.isInBailiwick(authority, domain));
    }

    @Test
    void throwsOnNullAuthority() {
        DnsNameDom domain = DnsNameDom.labels("example", "com");

        assertThrows(NullPointerException.class, () -> ResolveUtils.isInBailiwick(null, domain));
    }

    @Test
    void throwsOnNullDomain() {
        DnsNameDom authority = DnsNameDom.labels("example", "com");

        assertThrows(NullPointerException.class, () -> ResolveUtils.isInBailiwick(authority, null));
    }

}
