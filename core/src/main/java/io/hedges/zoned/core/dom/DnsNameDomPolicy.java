// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom;

import java.util.Arrays;
import java.util.List;

public interface DnsNameDomPolicy {

    boolean equalNames(DnsNameDom a, DnsNameDom b);

    int hashName(DnsNameDom name);

    void validateOrThrow(DnsNameDom name);

    enum Builtin implements DnsNameDomPolicy {
        HOSTNAME {

            private static final boolean[] allowedInHostnames = new boolean[256];

            static {
                for (int c = 'a'; c <= 'z'; c++) {
                    allowedInHostnames[c] = true;
                }
                for (int c = 'A'; c <= 'Z'; c++) {
                    allowedInHostnames[c] = true;
                }
                for (int c = '0'; c <= '9'; c++) {
                    allowedInHostnames[c] = true;
                }
                allowedInHostnames['-'] = true;
            }

            @Override
            public boolean equalNames(DnsNameDom a, DnsNameDom b) {
                if (a == b) {
                    return true;
                }
                if (a.size() != b.size()) {
                    return false;
                }
                final List<byte[]> aLabels = a.labels();
                final List<byte[]> bLabels = b.labels();

                for (int i = 0; i < aLabels.size(); i++) {
                    byte[] abs = aLabels.get(i);
                    byte[] bbs = bLabels.get(i);
                    for (int j = 0; j < abs.length; j++) {
                        // speed optimisation - wrote it before designing
                        // the code below, it's not necessary for correct-
                        // ness
                        if (abs[j] == bbs[j]) {
                            continue;
                        }
                        // or-ing with 0x20 has no effect on a-z 0-9 or '-'
                        // for upper case latin chars it maps them to
                        // lower case equiv
                        if ((abs[j] | 0x20) != (bbs[j] | 0x20)) {
                            return false;
                        }
                    }
                }
                return true;
            }

            @Override
            public int hashName(DnsNameDom name) {
                final int outerPrime = 31;
                final int innerPrime = 57;
                int result = 1;
                for (byte[] label : name.labels()) {
                    int hash = 0;
                    for (byte b : label) {
                        int c = (b & 0xFF) | 0x20; // fold to lowercase
                        hash = hash * innerPrime + c;
                    }
                    result = outerPrime * result + hash;
                }

                return result;
            }

            @Override
            public void validateOrThrow(DnsNameDom name) {
                for (byte[] arr : name.labels()) {
                    if (arr.length == 0) {
                        throw new IllegalArgumentException("Hostname labels can't be zero length");
                    }
                    if (arr[0] == '-' || arr[arr.length - 1] == '-') {
                        throw new IllegalArgumentException("Hostname labels can't begin or finish with a hypen");
                    }
                    for (byte b : arr) {
                        if (!allowedInHostnames[b & 0xFF]) {
                            throw new IllegalArgumentException(
                                    "Hostname labels can't have char value " + String.format("%02X", b));
                        }
                    }
                }
            }
        },
        PROTOCOL {

            @Override
            public boolean equalNames(DnsNameDom a, DnsNameDom b) {
                if (a == b) {
                    return true;
                }
                if (a.size() != b.size()) {
                    return false;
                }
                final List<byte[]> aLabels = a.labels();
                final List<byte[]> bLabels = b.labels();

                for (int i = 0; i < aLabels.size(); i++) {
                    if (!Arrays.equals(aLabels.get(i), bLabels.get(i))) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public int hashName(DnsNameDom name) {
                final int outerPrime = 31;
                final int innerPrime = 57;
                int result = 1;
                for (byte[] label : name.labels()) {
                    int hash = 0;
                    for (byte b : label) {
                        int c = b & 0xFF;
                        hash = hash * innerPrime + c;
                    }
                    result = outerPrime * result + hash;
                }

                return result;
            }

            @Override
            public void validateOrThrow(DnsNameDom name) {
                for (byte[] arr : name.labels()) {
                    if (arr.length == 0) {
                        throw new IllegalArgumentException("Labels can't be zero length");
                    }
                }
            }

        }
    }

}
