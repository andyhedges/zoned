// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom;

public enum DnsRecordClassDom {
    IN(1),
    CHAOS(3),
    HESIOD(4),
    NONE(254),
    ANY(255);

    private static final int MAX_U16 = 0xFFFF;
    private static final DnsRecordClassDom[] BY_CODE = new DnsRecordClassDom[MAX_U16 + 1];

    static {
        for (DnsRecordClassDom recordClass : values()) {
            if (recordClass.code < 0 || recordClass.code > MAX_U16) {
                throw new IllegalStateException("Record class code out of range: " + recordClass.code);
            }
            BY_CODE[recordClass.code] = recordClass;
        }
    }

    private final int code;

    DnsRecordClassDom(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static DnsRecordClassDom fromCode(int code) {
        if (code < 0 || code > MAX_U16) {
            throw new UnsupportedOperationException("Unsupported DNS record class: " + code);
        }
        DnsRecordClassDom recordClass = BY_CODE[code];
        if (recordClass == null) {
            throw new UnsupportedOperationException("Unsupported DNS record class: " + code);
        }
        return recordClass;
    }
}
