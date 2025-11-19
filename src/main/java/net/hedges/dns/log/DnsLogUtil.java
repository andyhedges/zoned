package net.hedges.dns.log;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.dns.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

public final class DnsLogUtil {

    private DnsLogUtil() {
    }

    public static Map<String, Object> toLog(DatagramDnsQuery query) {
        Map<String, Object> root = new LinkedHashMap<>();

        root.put("direction", "query");
        root.put("id", query.id());
        root.put("opCode", query.opCode().toString());
        root.put("recursionDesired", query.isRecursionDesired());
        root.put("z", query.z());

        root.put("sender", addressToMap(query.sender()));
        root.put("recipient", addressToMap(query.recipient()));

        root.put("questions", extractQuestions(query));
        root.put("counts", Map.of(
                "question", query.count(DnsSection.QUESTION),
                "answer", query.count(DnsSection.ANSWER),
                "authority", query.count(DnsSection.AUTHORITY),
                "additional", query.count(DnsSection.ADDITIONAL)
        ));

        return root;
    }

    private static List<Map<String, Object>> extractQuestions(DatagramDnsQuery query) {
        int count = query.count(DnsSection.QUESTION);
        List<Map<String, Object>> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            DnsQuestion q = query.recordAt(DnsSection.QUESTION, i);
            Map<String, Object> qMap = new LinkedHashMap<>();
            qMap.put("name", q.name());
            qMap.put("type", q.type().name());
            qMap.put("class", String.format("%s (%d)", className(q.dnsClass()), q.dnsClass()));
            list.add(qMap);
        }
        return list;
    }

    private static String className(int code) {
        return switch (code) {
            case 1 -> "IN";
            case 3 -> "CH";
            case 4 -> "HS";
            case 254 -> "NONE";
            case 255 -> "ANY";
            default -> "UNKNOWN";
        };
    }


    public static Map<String, Object> toResponseLog(DatagramDnsResponse resp) {
        Map<String, Object> root = new LinkedHashMap<>();

        root.put("direction", "response");

        // Header
        root.put("id", resp.id());
        root.put("opCode", resp.opCode().toString());
        root.put("rcode", resp.code().toString());

        // Flags that exist on responses
        root.put("authoritativeAnswer", resp.isAuthoritativeAnswer());
        root.put("truncated", resp.isTruncated());
        root.put("recursionDesired", resp.isRecursionDesired());
        root.put("recursionAvailable", resp.isRecursionAvailable());
        root.put("z", resp.z());

        // Transport info
        root.put("sender", addressToMap(resp.sender()));
        root.put("recipient", addressToMap(resp.recipient()));

        // Section counts
        root.put("questionCount", resp.count(DnsSection.QUESTION));
        root.put("answerCount", resp.count(DnsSection.ANSWER));
        root.put("authorityCount", resp.count(DnsSection.AUTHORITY));
        root.put("additionalCount", resp.count(DnsSection.ADDITIONAL));

        // Optional full record extraction
        root.put("questions", extractQuestions(resp));
        root.put("answers", extractRecords(resp, DnsSection.ANSWER));
        root.put("authority", extractRecords(resp, DnsSection.AUTHORITY));
        root.put("additional", extractRecords(resp, DnsSection.ADDITIONAL));

        return root;
    }

    private static Map<String, Object> addressToMap(SocketAddress address) {
        if (address instanceof InetSocketAddress inet) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("ip", inet.getAddress().getHostAddress());
            map.put("port", inet.getPort());
            return map;
        }
        return Map.of("raw", address.toString());
    }

    private static List<Map<String, Object>> extractQuestions(DnsMessage msg) {
        int count = msg.count(DnsSection.QUESTION);
        List<Map<String, Object>> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            DnsQuestion q = (DnsQuestion) msg.recordAt(DnsSection.QUESTION, i);
            Map<String, Object> qMap = new LinkedHashMap<>();
            qMap.put("name", q.name());
            qMap.put("type", q.type().name());
            qMap.put("class", q.dnsClass());
            list.add(qMap);
        }
        return list;
    }

    private static List<Map<String, Object>> extractRecords(DnsMessage msg, DnsSection section) {
        int count = msg.count(section);
        List<Map<String, Object>> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            DnsRecord r = msg.recordAt(section, i);
            Map<String, Object> rMap = new LinkedHashMap<>();
            rMap.put("name", r.name());
            rMap.put("type", r.type().name());
            rMap.put("class", r.dnsClass());
            rMap.put("ttl", r.timeToLive());

            if (r instanceof DnsRawRecord raw) {
                ByteBuf buf = raw.content();
                byte[] data = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), data);
                rMap.put("rawRdata", Base64.getEncoder().encodeToString(data));
            }

            list.add(rMap);
        }
        return list;
    }

}

