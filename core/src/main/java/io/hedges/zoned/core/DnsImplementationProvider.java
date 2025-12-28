// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core;

public interface DnsImplementationProvider {

    public DnsServer server();

    public DnsClient client();

}
