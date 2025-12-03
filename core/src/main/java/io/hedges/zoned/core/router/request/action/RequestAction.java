package io.hedges.zoned.core.router.request.action;

import io.hedges.zoned.core.dom.DnsMessageDom;

/**
 * Declarative description of what the resolver should do next
 * for a given DNS request.
 *
 * This interface has a small closed set of subtypes which cover
 * all the high level resolver behaviours.
 */
public sealed interface RequestAction
        permits AnswerLocallyAction,
        ForwardAction,
        ResolveRecursivelyAction,
        DropAction,
        RefuseAction {

    // Convenience factories so callers do not need to know subtype names.

    static RequestAction answerLocally(DnsMessageDom answer) {
        return new AnswerLocallyAction(answer);
    }

    static RequestAction forward(String upstreamName) {
        return new ForwardAction(upstreamName);
    }

    static RequestAction resolveRecursively(String strategyName) {
        return new ResolveRecursivelyAction(strategyName);
    }

    static RequestAction drop() {
        return new DropAction();
    }

    static RequestAction refuse() {
        return new RefuseAction();
    }
}
