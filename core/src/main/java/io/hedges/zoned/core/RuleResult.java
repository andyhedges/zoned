package io.hedges.zoned.core;

import java.util.List;

public final class RuleResult {

    public enum Disposition {
        CONTINUE,
        TERMINATE
    }

    private final List<DnsAction> actions;
    private final Disposition disposition;
    private final int priority;

    public RuleResult(List<DnsAction> actions,
                      Disposition disposition,
                      int priority) {
        this.actions = List.copyOf(actions);
        this.disposition = disposition;
        this.priority = priority;
    }

    public List<DnsAction> getActions() {
        return actions;
    }

    public Disposition getDisposition() {
        return disposition;
    }

    public int getPriority() {
        return priority;
    }
}
