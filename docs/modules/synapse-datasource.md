# synapse-datasource

This module provides datasource health, discovery, diagnostics, load-balancing decisions, and failover decisions.

Datasource creation, grouping, context management, and actual switching are delegated to the official baomidou dynamic-datasource integration.

Synapse Framework does not provide a custom datasource switching annotation, routing scope, routing context, advisor, or proxy creator.

Applications should use the official dynamic-datasource configuration and annotation model directly.
