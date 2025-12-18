package io.hedges.zoned.core.dom;

public enum DnsResponseCodeDom {
    NO_ERROR(0),
    FORMAT_ERROR(1),
    SERVER_FAILURE(2),
    NAME_ERROR(3),
    NOT_IMPLEMENTED(4),
    REFUSED(5),
    YXDOMAIN(6),
    YXRRSET(7),
    NXRRSET(8),
    NOTAUTH(9),
    NOTZONE(10),
    BADVERS(16),
    BADKEY(17),
    BADTIME(18),
    BADMODE(19),
    BADNAME(20),
    BADALG(21),
    BADTRUNC(22),
    BADCOOKIE(23);

    private static final int MAX_RCODE = 4095;
    private static final DnsResponseCodeDom[] BY_CODE = new DnsResponseCodeDom[MAX_RCODE + 1];

    static {
        for (DnsResponseCodeDom responseCode : values()) {
            if (responseCode.code < 0 || responseCode.code > MAX_RCODE) {
                throw new IllegalStateException("Response code out of range: " + responseCode.code);
            }
            BY_CODE[responseCode.code] = responseCode;
        }
    }

    private final int code;

    DnsResponseCodeDom(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static DnsResponseCodeDom fromCode(int code) {
        if (code < 0 || code > MAX_RCODE) {
            throw new UnsupportedOperationException("Unsupported DNS response code: " + code);
        }
        DnsResponseCodeDom responseCode = BY_CODE[code];
        if (responseCode == null) {
            throw new UnsupportedOperationException("Unsupported DNS response code: " + code);
        }
        return responseCode;
    }
}
