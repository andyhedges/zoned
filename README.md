# Zoned

<p><img src="logo.png"></p>

**Zoned** is a programmable DNS server built around a clean domain model, an asynchronous pipeline, and a rule engine
that determines how each query should be resolved. It separates DNS message handling, decision logic, network IO,
and upstream resolution into distinct, testable layers.

Zoned is designed to act as:

- a recursive resolver
- a forwarding resolver
- an authoritative server for selected zones
- a programmable DNS firewall or filter
- a hybrid resolver with rule-based routing

The internal architecture uses a strict domain-driven design so DNS messages are represented using rich domain objects
rather than the underlying DNS implemetations' codecs. Currently the implementation is Netty, but the design allows for
this to be swapped relatively easily


