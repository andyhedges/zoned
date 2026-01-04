// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class DnsResourceRecordDom {

    private DnsNameDom name;
    private DnsRecordTypeDom type;
    private DnsRecordClassDom recordClass;
    private int ttlSeconds;
    private RDataDom rdata;
}
