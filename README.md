# Zoned

[![Build](https://github.com/andyhedges/zoned/actions/workflows/build.yaml/badge.svg)](https://github.com/andyhedges/zoned/actions/workflows/build.yaml)
[![CodeQL](https://github.com/andyhedges/zoned/actions/workflows/codeql.yml/badge.svg)](https://github.com/andyhedges/zoned/actions/workflows/codeql.yml)
[![Coverage](https://img.shields.io/codecov/c/github/andyhedges/zoned)](https://codecov.io/gh/andyhedges/zoned)
[![Release](https://img.shields.io/github/v/release/andyhedges/zoned)](https://github.com/andyhedges/zoned/releases)
[![License](https://img.shields.io/github/license/andyhedges/zoned)](LICENSE)
[![Java](https://img.shields.io/badge/java-21+-blue)](https://adoptium.net/)
[![DNS](https://img.shields.io/badge/DNS-UDP%20%7C%20TCP-informational)](#)

> ⚠️ **Status: early, incomplete, and not functional**
>
> This repository is public primarily to enable GitHub features that require public visibility, such as Actions, code scanning, and dependency tooling.
> Zoned is under active development and should not be considered usable, stable, or complete.
> The code may not build, run, or behave correctly, and interfaces are expected to change substantially.

**Zoned** is an experimental programmable DNS server exploring a clean domain-driven approach to DNS resolution.
It is focused on separating DNS concepts and decision logic from transport, codecs, and IO concerns.

The long-term goal is to support use cases such as:

- a recursive resolver
- a forwarding resolver
- an authoritative server for selected zones
- a programmable DNS firewall or filter
- a hybrid resolver with rule-based routing

Internally, Zoned uses a strict domain model where DNS messages are represented as rich domain objects rather than being tightly coupled to a specific DNS codec or wire format.
The current implementation uses Netty, but the architecture is intentionally structured so that the underlying DNS and networking implementation can be replaced.

At present, this project should be treated as a work-in-progress design and research effort rather than a functioning DNS server.

## License

Zoned is licensed under the Apache License, Version 2.0.

See the [LICENSE](LICENSE) file for the full license text  
and the [NOTICE](NOTICE) file for copyright and attribution information.

Contributions are accepted under the same license.




