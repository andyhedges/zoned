package net.hedges.dns;

import io.netty.handler.codec.dns.DnsRecordType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Zone {
    private final Map<String, List<ZoneRecord>> records = new ConcurrentHashMap<>();

    public void addA(String name, String addr, long ttl) {
        records.computeIfAbsent(name.toLowerCase(), k -> new CopyOnWriteArrayList<>())
                .add(new ZoneRecord(DnsRecordType.A, addr, ttl));
    }

    public List<ZoneRecord> find(String name, DnsRecordType type) {
        List<ZoneRecord> all = records.getOrDefault(name.toLowerCase(), List.of());
        return all.stream().filter(r -> r.type() == type).toList();
    }

    public static Zone example() {
        Zone z = new Zone();
        z.addA("example.local.", "192.168.1.10", 60);
        z.addA("example.local.", "192.168.1.11", 120);
        z.addA("router.local.", "192.168.1.1", 120);
        return z;
    }
}

