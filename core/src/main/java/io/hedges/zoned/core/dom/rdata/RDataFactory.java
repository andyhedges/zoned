// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.RDataDom;

public class RDataFactory {

    public static RDataDom fromBytes(DnsRecordTypeDom type, byte[] bytes) {
        return fromBytes(type, bytes, null);
    }

    public static RDataDom fromBytes(DnsRecordTypeDom type, byte[] bytes, NameResolver resolver) {
        return switch (type) {
            case A -> ARecordDataDom.from(bytes);
            case AAAA -> AAAARecordDataDom.from(bytes);
            case AFSDB -> AfsDbRecordDataDom.from(bytes, resolver);
            case ANY -> AnyRecordDataDom.from(bytes);
            case APL -> AplRecordDataDom.from(bytes);
            case AXFR -> AxfrRecordDataDom.from(bytes);
            case CAA -> CaaRecordDataDom.from(bytes);
            case CNAME -> CnameRecordDataDom.from(bytes, resolver);
            case CDS -> CdsRecordDataDom.from(bytes);
            case CDNSKEY -> CdnskeyRecordDataDom.from(bytes);
            case CERT -> CertRecordDataDom.from(bytes);
            case CSYNC -> CsyncRecordDataDom.from(bytes);
            case DHCID -> DhcidRecordDataDom.from(bytes);
            case DLV -> DlvRecordDataDom.from(bytes);
            case DNAME -> DnameRecordDataDom.from(bytes, resolver);
            case DNSKEY -> DnskeyRecordDataDom.from(bytes);
            case DS -> DsRecordDataDom.from(bytes);
            case EUI48 -> Eui48RecordDataDom.from(bytes);
            case EUI64 -> Eui64RecordDataDom.from(bytes);
            case HIP -> HipRecordDataDom.from(bytes, resolver);
            case HTTPS -> HttpsRecordDataDom.from(bytes);
            case IPSECKEY -> IpseckeyRecordDataDom.from(bytes);
            case KEY -> KeyRecordDataDom.from(bytes);
            case KX -> KxRecordDataDom.from(bytes, resolver);
            case LOC -> LocRecordDataDom.from(bytes);
            case MX -> MxRecordDataDom.from(bytes, resolver);
            case NAPTR -> NaptrRecordDataDom.from(bytes);
            case NS -> NsRecordDataDom.from(bytes, resolver);
            case NSEC -> NsecRecordDataDom.from(bytes);
            case NSEC3 -> Nsec3RecordDataDom.from(bytes);
            case NSEC3PARAM -> Nsec3ParamRecordDataDom.from(bytes);
            case OPENPGPKEY -> OpenPgpKeyRecordDataDom.from(bytes);
            case OPT -> OptRecordDataDom.from(bytes);
            case PTR -> PtrRecordDataDom.from(bytes, resolver);
            case RP -> RpRecordDataDom.from(bytes);
            case RRSIG -> RrsigRecordDataDom.from(bytes);
            case SIG -> SigRecordDataDom.from(bytes);
            case SMIMEA -> SmimeaRecordDataDom.from(bytes);
            case SOA -> SoaRecordDataDom.from(bytes, resolver);
            case SSHFP -> SshfpRecordDataDom.from(bytes);
            case SRV -> SrvRecordDataDom.from(bytes);
            case SVCB -> SvcbRecordDataDom.from(bytes);
            case TA -> TaRecordDataDom.from(bytes);
            case TKEY -> TkeyRecordDataDom.from(bytes);
            case TLSA -> TlsaRecordDataDom.from(bytes);
            case TSIG -> TsigRecordDataDom.from(bytes);
            case TXT -> TxtRecordDataDom.from(bytes);
            case URI -> UriRecordDataDom.from(bytes);
            case ZONEMD -> ZonemdRecordDataDom.from(bytes);
            case null -> throw new IllegalArgumentException("Null byte array");
        };
    }

    public static RDataDom fromWire(DnsRecordTypeDom type, byte[] bytes, NameResolver resolver) {
        return fromBytes(type, bytes, resolver);
    }
}
