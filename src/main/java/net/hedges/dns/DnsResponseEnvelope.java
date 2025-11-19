package net.hedges.dns;

import io.netty.handler.codec.dns.*;
import io.netty.util.ReferenceCounted;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public final class DnsResponseEnvelope {
    private final int id;
    private final DnsResponseCode rcode;
    private final DnsQuestion question;
    private final List<DnsRecord> answers;
    private final List<DnsRecord> authorities;
    private final List<DnsRecord> additionals;

    // constructor + getters

    public static DnsResponseEnvelope fromNetty(DnsMessage msg) {
        DnsQuestion q = msg.recordAt(DnsSection.QUESTION);
        // DnsQuestion is not ref-counted, safe to reuse as-is

        List<DnsRecord> answers = copySection(msg, DnsSection.ANSWER);
        List<DnsRecord> auth    = copySection(msg, DnsSection.AUTHORITY);
        List<DnsRecord> add     = copySection(msg, DnsSection.ADDITIONAL);

        DnsResponseCode rcode =
                (msg instanceof DnsResponse)
                        ? ((DnsResponse) msg).code()
                        : DnsResponseCode.NOERROR;

        return new DnsResponseEnvelope(
                msg.id(),
                rcode,
                q,
                answers,
                auth,
                add
        );
    }

    private static List<DnsRecord> copySection(DnsMessage msg, DnsSection sec) {
        List<DnsRecord> out = new ArrayList<>();
        int count = msg.count(sec);
        for (int i = 0; i < count; i++) {
            DnsRecord rec = msg.recordAt(sec, i);
            if (rec != null) {
                // Only concrete implementations that hold ByteBufs are ref-counted.
                // Bump their refCnt so they survive auto-release of the inbound message.
                if (rec instanceof ReferenceCounted ref) {
                    ref.retain();
                }
                out.add(rec);
            }
        }
        return out;
    }

}

