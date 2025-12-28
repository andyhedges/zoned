// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.netty;

import io.hedges.zoned.core.dom.DnsMessageDom;

import java.net.InetSocketAddress;

public record UdpDnsOutbound(DnsMessageDom message, InetSocketAddress recipient) {
}
