package io.hedges.zoned.core.router.request.action;

public record ForwardAction(String upstreamName) implements RequestAction {

    public ForwardAction {
        if (upstreamName == null || upstreamName.isBlank()) {
            throw new IllegalArgumentException("upstreamName must not be null or blank");
        }
    }
}
