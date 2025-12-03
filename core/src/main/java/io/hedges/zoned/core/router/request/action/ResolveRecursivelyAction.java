package io.hedges.zoned.core.router.request.action;

public record ResolveRecursivelyAction(String strategyName) implements RequestAction {

    public ResolveRecursivelyAction {
        if (strategyName == null || strategyName.isBlank()) {
            throw new IllegalArgumentException("strategyName must not be null or blank");
        }
    }
}
