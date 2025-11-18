package net.hedges.dns;

import io.netty.handler.codec.dns.DnsRecordType;

public record ZoneRecord(DnsRecordType type, String data, long ttl) {}

