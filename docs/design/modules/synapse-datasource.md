# synapse-datasource design

The module provides datasource discovery, descriptors, health checks, role detection, diagnostics, load-balancing decisions, and failover decisions.

Actual datasource creation and switching remain the responsibility of the official baomidou dynamic-datasource integration.

The framework must not define a custom switching annotation, routing scope, routing context, route selector, route resolver, advisor, or proxy creator.

`DataSourceRouter` is a decision model only. It must not modify the dynamic-datasource context.
