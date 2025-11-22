package io.hedges.zoned.core;

import java.util.Locale;

public final class DnsName {
    private final String value;

    public DnsName(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }
        this.value = value.toLowerCase(Locale.ROOT);
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
