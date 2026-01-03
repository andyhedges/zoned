// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.cache;

import java.util.Optional;

public interface Cache<K, V> {
    
    public void put(K k, V v);

    public Optional<V> get(K k);

}
