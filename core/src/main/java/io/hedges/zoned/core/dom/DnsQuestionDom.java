// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class DnsQuestionDom {
    private DnsNameDom name;
    private DnsRecordTypeDom recordType;
    private DnsRecordClassDom recordClass;
}
