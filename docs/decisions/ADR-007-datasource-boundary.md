# ADR-007: Datasource boundary

## Status

Accepted

## Context

Baomidou dynamic-datasource already owns datasource creation, grouping, annotations, context management, and actual switching.

## Decision

`synapse-datasource` provides discovery, descriptors, type and role detection, health checks, candidate filtering, load-balancing decisions, and failover decisions.

Applications use the official dynamic-datasource configuration and switching model directly.

The framework does not provide `UseDatasource`, custom routing contexts or scopes, route selectors or resolvers, a runtime datasource-definition registry, or its own auto-proxy creator.

`DataSourceRouter` is a decision model only and never changes the dynamic-datasource context.

## Consequences

There is one datasource switching mechanism. Framework governance cannot conflict with transaction advisors or duplicate the underlying routing engine.

## Rejected alternatives

- A second datasource switching annotation.
- A framework-owned datasource context stack.
- A framework-owned AOP proxy creator.
- Default Seata integration.

## Date

2026-06-19
