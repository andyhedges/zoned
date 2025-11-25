package io.hedges.zoned.core.dom;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class DnsHeaderDom {

    private int id;
    private boolean response;
    private DnsOpCodeDom opCode;
    private boolean authoritativeAnswer;
    private boolean truncation;
    private boolean recursionDesired;
    private boolean recursionAvailable;
    private boolean authenticatedData;
    private boolean checkingDisabled;
    private DnsResponseCodeDom responseCode;

}
