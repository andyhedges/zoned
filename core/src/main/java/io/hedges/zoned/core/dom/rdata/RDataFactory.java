package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.RDataDom;

public class RDataFactory {

    public static RDataDom fromBytes(DnsRecordTypeDom type, byte[] bytes) {
        return switch (type) {
            case A -> ARecordDataDom.from(bytes);
            case AAAA -> AAAARecordDataDom.from(bytes);
            case DS -> DsRecordDataDom.from(bytes);
            case MX -> MxRecordDataDom.from(bytes);
            case NS -> NsRecordDataDom.from(bytes);
            case OPT -> OptRecordDataDom.from(bytes);
            case PTR -> PtrRecordDataDom.from(bytes);
            case SOA -> SoaRecordDataDom.from(bytes);
            case SRV -> SrvRecordDataDom.from(bytes);
            case TXT -> TxtRecordDataDom.from(bytes);
            case NSEC -> NsecRecordDataDom.from(bytes);
            case CNAME -> CnameRecordDataDom.from(bytes);
            case RRSIG -> RrsigRecordDataDom.from(bytes);
            case DNSKEY -> DnskeyRecordDataDom.from(bytes);
            case NSEC3 -> Nsec3RecordDataDom.from(bytes);
            case null -> throw new IllegalArgumentException("Null byte array");
        };
    }
}
