// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom;

import lombok.Builder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Builder
public final class DnsNameDom {

    @Builder.Default
    private final List<byte[]> labels = Collections.emptyList();

    @Builder.Default
    private final DnsNameDomPolicy policy = DnsNameDomPolicy.Builtin.PROTOCOL;

    public static final DnsNameDom ROOT = DnsNameDom.builder().build();

    public static DnsNameDom labels(List<String> labels) {
        Objects.requireNonNull(labels, "labels cannot be null");
        return DnsNameDom.builder().labelStrings(labels).build();
    }

    public static DnsNameDom labels(String... labels) {
        Objects.requireNonNull(labels, "labels cannot be null");
        return DnsNameDom.builder().labelStrings(Arrays.asList(labels)).build();
    }

    public List<byte[]> labels() {
        return labels;
    }

    public byte[] label(int index) {
        return labels.get(index);
    }

    public List<String> labelStrings() {
        List<String> decoded = new ArrayList<>(labels.size());
        for (byte[] label : labels) {
            decoded.add(new String(label, StandardCharsets.US_ASCII));
        }
        return decoded;
    }

    public int size() {
        return labels.size();
    }

    public String toString() {
        return String.join(".", this.labelStrings()) + ".";
    }

    public boolean endsWith(DnsNameDom potentialSuffix) {
        Objects.requireNonNull(potentialSuffix, "potentialSuffix");
        if (!Objects.equals(this.policy, potentialSuffix.policy)) {
            return false;
        }
        int suffixSize = potentialSuffix.size();
        int nameSize = this.size();
        if (suffixSize > nameSize) {
            return false;
        }
        int offset = nameSize - suffixSize;
        List<byte[]> suffixLabels = new ArrayList<>(labels.subList(offset, nameSize));
        DnsNameDom tail = DnsNameDom.builder()
                .policy(this.policy)
                .labels(suffixLabels)
                .build();
        return this.policy.equalNames(tail, potentialSuffix);
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DnsNameDom)) {
            return false;
        }
        final DnsNameDom that = (DnsNameDom) o;
        if (!this.policy.equals(that.policy)) {
            return false;
        }
        return this.policy.equalNames(this, that);
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + policy.hashCode();
        result = prime * result + policy.hashName(this);
        return result;
    }

    public static class DnsNameDomBuilder {
        public DnsNameDomBuilder labelStrings(List<String> labels) {
            Objects.requireNonNull(labels, "labels cannot be null");
            this.labels(toLabelBytes(labels));
            return this;
        }

        public DnsNameDomBuilder labelStrings(String... labels) {
            Objects.requireNonNull(labels, "labels cannot be null");
            return labelStrings(Arrays.asList(labels));
        }
    }

    private static List<byte[]> toLabelBytes(List<String> labels) {
        List<byte[]> encoded = new ArrayList<>(labels.size());
        for (String label : labels) {
            if (label == null) {
                throw new IllegalArgumentException("label cannot be null");
            } else {
                encoded.add(label.getBytes(StandardCharsets.US_ASCII));
            }
        }
        return encoded;
    }

}
