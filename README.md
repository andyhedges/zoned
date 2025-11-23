# Zoned
<p><img src="logo.png"></p>

Zoned is a rules based programmable DNS server focused on flexibility, composability, and clarity. It aims to do for DNS what modern reverse proxies do for HTTP: provide a small, sharp core with powerful extension points.

## What Zoned Does

- Acts as a recursive resolver when you need full resolution
- Proxies selected queries upstream
- Serves authoritative zones where configured
- Lets you mix these modes on a per-query basis

This makes it useful for homelab setups, split-horizon networks, edge deployments, and developer workflows where DNS needs to be dynamic and programmable.

## Key Features

- Fast UDP DNS handling
- Optional TCP fallback
- Rule based routing
- Pluggable handlers for different record sources
- Extensible architecture designed for Java/Netty

## Architecture Overview

1. **Listener** handles UDP and TCP queries.
2. **Router** decides whether to recurse, proxy, or answer.
3. **Handlers** resolve a query using the appropriate strategy.
4. **Response Builder** constructs RFC compliant replies.

## Example Configuration

```yaml
listen:
  - 0.0.0.0:53

routes:
  - match: "*.lan"
    mode: authoritative
  - match: "internal.example.com"
    mode: proxy
    upstream: 1.1.1.1
  - match: "*"
    mode: recursive
```

## Rule Based Routing

Zoned evaluates incoming queries against an ordered list of rules. Each rule has three parts:

- **match** defines which queries it applies to. This can be a wildcard domain, a specific name, or a predicate.
- **mode** determines how the query is resolved. Options include authoritative, proxy, and recursive.
- **params** supply extra information, such as upstream servers.

Rules are evaluated top to bottom. The first match wins. This keeps behaviour predictable and avoids hidden fallthroughs.

Examples of match patterns:

- `example.com` exact match
- `*.lan` wildcard suffix match
- `=_type:AAAA` match based on record type

Handlers receive only the queries matched for them, making configuration composable and easy to reason about.

## Goals

- Provide predictable DNS behaviour
- Keep configuration simple
- Avoid unnecessary magic
- Stay composable instead of monolithic

## Non Goals

- Replacing full DNS servers like BIND or NSD for large scale authoritative hosting
- Acting as a heavy policy engine

## Building

```bash
./mvnw clean package
```

## Running

```bash
java -jar zoned.jar
```

## Status

Early development. Interfaces and behaviours are still malleable while the core feature set stabilises.

## License

MIT

