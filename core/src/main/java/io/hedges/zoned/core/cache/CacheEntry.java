// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CacheEntry {
    private Rrset rrset;
    private TrustLevel trustLevel;
    private boolean complete;
    private long expireAt;
}
