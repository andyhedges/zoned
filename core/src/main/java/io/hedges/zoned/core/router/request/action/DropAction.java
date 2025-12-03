package io.hedges.zoned.core.router.request.action;

/**
 * Intentionally send no response to the client for this query.
 */
public record DropAction() implements RequestAction {}
