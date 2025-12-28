// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom;

public enum DnsRecordTypeDom {
    A(1),
    NS(2),
    CNAME(5),
    SOA(6),
    PTR(12),
    MX(15),
    TXT(16),
    RP(17),
    AFSDB(18),
    SIG(24),
    KEY(25),
    AAAA(28),
    LOC(29),
    SRV(33),
    NAPTR(35),
    KX(36),
    CERT(37),
    DNAME(39),
    OPT(41),
    APL(42),
    DS(43),
    SSHFP(44),
    IPSECKEY(45),
    RRSIG(46),
    NSEC(47),
    DNSKEY(48),
    DHCID(49),
    NSEC3(50),
    NSEC3PARAM(51),
    TLSA(52),
    SMIMEA(53),
    HIP(55),
    CDS(59),
    CDNSKEY(60),
    OPENPGPKEY(61),
    CSYNC(62),
    ZONEMD(63),
    SVCB(64),
    HTTPS(65),
    EUI48(108),
    EUI64(109),
    TKEY(249),
    TSIG(250),
    AXFR(252),
    ANY(255),
    URI(256),
    CAA(257),
    TA(32768),
    DLV(32769);

    private static final int MAX_U16 = 0xFFFF;
    private static final DnsRecordTypeDom[] BY_CODE = new DnsRecordTypeDom[MAX_U16 + 1];

    static {
        for (DnsRecordTypeDom type : values()) {
            if (type.code < 0 || type.code > MAX_U16) {
                throw new IllegalStateException("Record type code out of range: " + type.code);
            }
            BY_CODE[type.code] = type;
        }
    }

    private final int code;

    DnsRecordTypeDom(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static DnsRecordTypeDom fromCode(int code) {
        if (code < 0 || code > MAX_U16) {
            throw new UnsupportedOperationException("Unsupported DNS record type: " + code);
        }
        DnsRecordTypeDom type = BY_CODE[code];
        if (type == null) {
            throw new UnsupportedOperationException("Unsupported DNS record type: " + code);
        }
        return type;
    }
}
