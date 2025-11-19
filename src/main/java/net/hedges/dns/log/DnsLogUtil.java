package net.hedges.dns.log;

import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsSection;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DnsLogUtil {

    private DnsLogUtil() {}

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

    private static Map<String, Object> addressToMap(SocketAddress address) {
        if (address instanceof InetSocketAddress inet) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("host", inet.getAddress().getHostAddress());
            map.put("port", inet.getPort());
            return map;
        }
        return Map.of("raw", address.toString());
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
}

