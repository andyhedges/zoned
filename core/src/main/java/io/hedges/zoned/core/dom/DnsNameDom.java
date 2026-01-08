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
        int suffixSize = potentialSuffix.size();
        int nameSize = this.size();
        if (suffixSize > nameSize) {
            return false;
        }
        int offset = nameSize - suffixSize;
        for (int i = 0; i < suffixSize; i++) {
            if (!Arrays.equals(potentialSuffix.label(i), this.label(offset + i))) {
                return false;
            }
        }
        return true;
    }

    // lombok doesn't do a deep equals on byte arrays
    // we need that
    public boolean equals(final Object o) {
        //same object
        if (o == this) {
            return true;
        }
        if (!(o instanceof DnsNameDom)) {
            return false;
        }
        final DnsNameDom that = (DnsNameDom) o;
        if(this.size() != that.size()){
            return false;
        }
        final List<byte[]> thisLabels = this.labels();
        final List<byte[]> thatLabels = that.labels();

        for(int i = 0; i < thisLabels.size(); i++){
            byte[] a = thisLabels.get(i);
            byte[] b = thatLabels.get(i);
            if(!Arrays.equals(a, b)){
                return false;
            }
        }
        return true;
    }

    // lombok doesn't do a deep hashCode on byte arrays
    // we need that
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (byte[] label : labels) {
            result = prime * result + Arrays.hashCode(label);
        }
        return result;
    }

    public static class DnsNameDomBuilder {
        public DnsNameDomBuilder labelStrings(List<String> labels) {
            this.labels(toLabelBytes(Objects.requireNonNull(labels, "labels cannot be null")));
            return this;
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

    private DnsNameDom(List<byte[]> labels) {
        this.labels = labels;
    }
}
