package io.hedges.zoned.core.rule;

import java.util.List;

public record RuleResult(List<DnsAction> actions, Disposition disposition, int priority) {

    public enum Disposition {
        CONTINUE,
        TERMINATE
    }

    public RuleResult(List<DnsAction> actions,
                      Disposition disposition,
                      int priority) {
        this.actions = List.copyOf(actions);
        this.disposition = disposition;
        this.priority = priority;
    }
}
