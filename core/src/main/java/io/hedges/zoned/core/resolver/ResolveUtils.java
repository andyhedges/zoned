// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.resolver;

import io.hedges.zoned.core.dom.DnsNameDom;

public class ResolveUtils {

    public static boolean isInBailiwick(DnsNameDom authorityZone, DnsNameDom domain) {
        return domain.endsWith(authorityZone);
    }
}
