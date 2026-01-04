// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.cache;

import org.junit.jupiter.api.Test;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvictingCacheTest {

    @Test
    void getReturnsEmptyForMissingKey() {
        EvictingCache<String, String> cache = new EvictingCache<>();
        assertEquals(Optional.empty(), cache.get("missing"));
    }

    @Test
    void validityPolicyFiltersAndRemovesEntries() {
        EvictingCache<String, String> cache = new EvictingCache<>();
        cache.put("k", "v");
        cache.validityPolicy(v -> false);

        assertTrue(cache.get("k").isEmpty());

        cache.validityPolicy(v -> true);
        assertTrue(cache.get("k").isEmpty());
    }

    @Test
    void evictsWhenMaxEntriesExceeded() {
        EvictingCache<String, String> cache = new EvictingCache<>();
        for (int i = 0; i <= 100; i++) {
            cache.put("k" + i, "v" + i);
        }

        assertTrue(cache.get("k0").isEmpty());
        assertEquals(Optional.of("v100"), cache.get("k100"));
    }

    @Test
    void nonNullParametersThrowNullPointerException() {
        EvictingCache<String, String> cache = new EvictingCache<>();
        assertThrows(NullPointerException.class, () -> cache.put(null, "v"));
        assertThrows(NullPointerException.class, () -> cache.put("k", null));
        assertThrows(NullPointerException.class, () -> cache.get(null));
        assertThrows(NullPointerException.class, () -> cache.validityPolicy(null));
    }
}
