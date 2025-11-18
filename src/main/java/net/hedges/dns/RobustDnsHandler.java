package net.hedges.dns;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RobustDnsHandler extends SimpleChannelInboundHandler<DatagramDnsQuery> {

    private final Zone zone;

    public RobustDnsHandler(Zone zone) {
        this.zone = zone;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery query) {
        DatagramDnsResponse response =
                new DatagramDnsResponse(query.recipient(), query.sender(), query.id());

        try {
            if (query.opCode() != DnsOpCode.QUERY) {
                response.setCode(DnsResponseCode.NOTIMP);
                ctx.writeAndFlush(response);
                return;
            }

            if (query.count(DnsSection.QUESTION) == 0) {
                response.setCode(DnsResponseCode.FORMERR);
                ctx.writeAndFlush(response);
                return;
            }

            // For simplicity handle only first question
            DnsQuestion q = query.recordAt(DnsSection.QUESTION);
            response.addRecord(DnsSection.QUESTION, q);

            if (q.type() != DnsRecordType.A) {
                // Not supporting other types yet
                response.setCode(DnsResponseCode.NOTIMP);
                ctx.writeAndFlush(response);
                return;
            }

            List<ZoneRecord> records = zone.find(q.name(), q.type());
            if (records.isEmpty()) {
                response.setCode(DnsResponseCode.NXDOMAIN);
                ctx.writeAndFlush(response);
                return;
            }

            for (ZoneRecord zr : records) {
                ByteBuf content = encodeRecordData(ctx.alloc(), zr);
                DnsRawRecord rr = new DefaultDnsRawRecord(
                        q.name(),
                        zr.type(),
                        zr.ttl(),
                        content
                );
                response.addRecord(DnsSection.ANSWER, rr);
                System.out.printf("name: %s, type: %s, ttl: %d, content: %s\n", q.name(), zr.type(), zr.ttl(), ByteBufDnsUtil.ipv4FromBuf(content, 0));
            }

            response.setAuthoritativeAnswer(true);

            ctx.writeAndFlush(response);

        } catch (Exception e) {
            // Catch per request and fall back to SERVFAIL
            e.printStackTrace();
            safeServfail(ctx, query);
        }
    }

    private ByteBuf encodeRecordData(ByteBufAllocator alloc, ZoneRecord record) throws UnknownHostException {
        if (record.type() == DnsRecordType.A) {
            byte[] addr = InetAddress.getByName(record.data()).getAddress();
            return Unpooled.wrappedBuffer(addr);
        }
        // extend for AAAA, etc
        throw new UnsupportedOperationException("Unsupported record type: " + record.type());
    }

    private void safeServfail(ChannelHandlerContext ctx, DatagramDnsQuery query) {
        DatagramDnsResponse resp =
                new DatagramDnsResponse(query.recipient(), query.sender(), query.id());
        resp.setCode(DnsResponseCode.SERVFAIL);
        ctx.writeAndFlush(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Absolutely last resort
        cause.printStackTrace();
    }
}

