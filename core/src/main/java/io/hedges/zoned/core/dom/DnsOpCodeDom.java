package io.hedges.zoned.core.dom;

public enum DnsOpCodeDom {
    QUERY(0),
    IQUERY(1),
    STATUS(2),
    NOTIFY(4),
    UPDATE(5);

    private static final DnsOpCodeDom[] BY_CODE = new DnsOpCodeDom[16];

    static {
        for (DnsOpCodeDom opCode : values()) {
            if (opCode.code < 0 || opCode.code >= BY_CODE.length) {
                throw new IllegalStateException("OpCode out of range: " + opCode.code);
            }
            BY_CODE[opCode.code] = opCode;
        }
    }

    private final int code;

    DnsOpCodeDom(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static DnsOpCodeDom fromCode(int code) {
        if (code < 0 || code >= BY_CODE.length) {
            throw new UnsupportedOperationException("Unsupported DNS opcode: " + code);
        }
        DnsOpCodeDom opCode = BY_CODE[code];
        if (opCode == null) {
            throw new UnsupportedOperationException("Unsupported DNS opcode: " + code);
        }
        return opCode;
    }
}
