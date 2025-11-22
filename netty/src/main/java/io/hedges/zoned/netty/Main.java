package io.hedges.zoned.netty;

public final class Main {

    public static void main(String[] args) throws Exception {
        int port = 53;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        UdpNettyDnsServer server = new UdpNettyDnsServer(port);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("zoned DNS server listening on UDP " + port);
    }
}
