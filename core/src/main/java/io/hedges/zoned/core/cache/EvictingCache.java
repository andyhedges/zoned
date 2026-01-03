// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.cache;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;

public class EvictingCache<K, V> implements Cache<K, V>{

    private static final int MAX_ENTRIES = 100;

    private Map<K, V> store = new LinkedHashMap<K, V>(16, 0.75f, true){
        @Override
        protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public synchronized Optional<V> get(K key) {
        return Optional.ofNullable(store.get(key));
    }

    public synchronized void put(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key and value must not be null");
        }
        store.put(key, value);
    }

}
