package io.hedges.zoned.core.domain;

import java.util.Locale;

public record DnsName(String value) {
    public DnsName(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }
        this.value = value.toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value;
    }
}
