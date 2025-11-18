package net.hedges.dns;

import io.netty.buffer.ByteBuf;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ByteBufDnsUtil {

    public static String ipv4FromBuf(ByteBuf buf, int index) throws UnknownHostException {
        byte[] addr = new byte[4];
        buf.getBytes(index, addr);  // doesn't change readerIndex
        InetAddress inetAddress = InetAddress.getByAddress(addr);
        return inetAddress.getHostAddress();
    }
}
