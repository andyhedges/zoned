package io.hedges.zoned.core;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface DnsClient {

    public CompletionStage<List<InetAddress>> lookup(String name);

}
