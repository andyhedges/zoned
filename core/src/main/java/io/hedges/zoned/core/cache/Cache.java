// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.cache;

import java.util.Optional;
import java.util.function.Predicate;
import lombok.NonNull;

public interface Cache<K, V> {
    
    public void put(@NonNull K k, @NonNull V v);

    public Optional<V> get(@NonNull K k);

    public void validityPolicy(@NonNull Predicate<V> policy);

}
