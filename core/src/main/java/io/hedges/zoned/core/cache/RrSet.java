// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.cache;

import io.hedges.zoned.core.dom.DnsResourceRecordDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class RrSet {
    private List<DnsResourceRecordDom> records;
}
