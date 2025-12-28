// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.router.request.action;

import io.hedges.zoned.core.dom.DnsMessageDom;

/**
 * Return a locally constructed DNS response without contacting any upstream.
 */
public record AnswerLocallyAction(DnsMessageDom answer) implements RequestAction {

    public AnswerLocallyAction {
        if (answer == null) {
            throw new IllegalArgumentException("answer must not be null");
        }
    }
}
