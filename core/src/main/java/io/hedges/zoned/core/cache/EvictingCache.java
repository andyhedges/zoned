// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.cache;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.NonNull;

public class EvictingCache<K, V> implements Cache<K, V> {

    private static final int MAX_ENTRIES = 100;
    private volatile Predicate<V> validity = v -> true;

    private final Map<K, V> store = new LinkedHashMap<K, V>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public synchronized Optional<V> get(@NonNull K key) {
        V value = store.get(key);
        if (value == null) {
            return Optional.empty();
        } else if (validity.test(value)) {
            return Optional.of(value);
        }
        store.remove(key);
        return Optional.empty();

    }

    public synchronized void put(@NonNull K key, @NonNull V value) {
        store.put(key, value);
    }

    @Override
    public void validityPolicy(@NonNull Predicate<V> validityPolicy) {
        this.validity = validityPolicy;
    }

}
