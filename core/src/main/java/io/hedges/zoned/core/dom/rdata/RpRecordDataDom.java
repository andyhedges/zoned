// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsNameDomPolicy;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS RP record RDATA.
 *
 * <p>RDATA is mailbox domain name and text domain name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | MboxDNAME | variable     | Mailbox domain name.       |
 * | TxtDNAME  | variable     | Text domain name.          |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Provides Responsible Person (RP) contact information and a related text domain.</p>
 * <p>The mailbox name encodes an email address, while the text domain can point to additional
 * descriptive information (often via a TXT record).</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc1183">RFC 1183</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class RpRecordDataDom implements RDataDom {
    private DnsNameDom mailbox;
    private DnsNameDom textDomain;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        if (rdata == null || rdata.length == 0) {
            throw new IllegalArgumentException("RP RDATA cannot be empty");
        }
        RDataUtils.DnsNameParseResult mailboxResult = RDataUtils.parseDnsName(
                rdata,
                0,
                resolver,
                DnsNameDomPolicy.Builtin.PROTOCOL);
        int idx = mailboxResult.nextIndex();
        if (idx >= rdata.length) {
            throw new IllegalArgumentException("RP RDATA missing text domain name");
        }
        RDataUtils.DnsNameParseResult textResult = RDataUtils.parseDnsName(
                rdata,
                idx,
                resolver,
                DnsNameDomPolicy.Builtin.PROTOCOL);
        if (textResult.nextIndex() != rdata.length) {
            throw new IllegalArgumentException("Extra bytes after RP text domain name");
        }
        return RpRecordDataDom.builder()
                .mailbox(mailboxResult.name())
                .textDomain(textResult.name())
                .build();
    }

    @Override
    public byte[] to() {
        if (mailbox == null) {
            throw new IllegalArgumentException("RP mailbox is null");
        }
        if (textDomain == null) {
            throw new IllegalArgumentException("RP text domain is null");
        }
        byte[] mailboxBytes = RDataUtils.toByteArray(mailbox);
        byte[] textBytes = RDataUtils.toByteArray(textDomain);
        byte[] out = new byte[mailboxBytes.length + textBytes.length];
        System.arraycopy(mailboxBytes, 0, out, 0, mailboxBytes.length);
        System.arraycopy(textBytes, 0, out, mailboxBytes.length, textBytes.length);
        return out;
    }
}
