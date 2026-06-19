package com.indigo.synapse.datasource.definition;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 数据源定义线程安全注册表。
 *
 * <p>刷新时按 Provider 顺序合并；重复 key 立即失败，避免静默覆盖数据源配置。</p>
 */
public final class DatasourceRegistry {

    private final List<DatasourceDefinitionProvider> providers;
    private final AtomicReference<Map<DatasourceKey, DatasourceDefinition>> definitions =
            new AtomicReference<>(Map.of());

    public DatasourceRegistry(Collection<DatasourceDefinitionProvider> providers) {
        this.providers = providers == null ? List.of() : providers.stream()
                .sorted(Comparator.comparingInt(DatasourceDefinitionProvider::order))
                .toList();
    }

    /** 原子刷新并返回新快照。 */
    public Map<DatasourceKey, DatasourceDefinition> refresh() {
        Map<DatasourceKey, DatasourceDefinition> merged = new LinkedHashMap<>();
        for (DatasourceDefinitionProvider provider : providers) {
            Collection<DatasourceDefinition> loaded = provider.load();
            if (loaded == null) {
                continue;
            }
            for (DatasourceDefinition definition : loaded) {
                DatasourceDefinition previous = merged.putIfAbsent(definition.key(), definition);
                if (previous != null) {
                    throw new IllegalStateException("duplicate datasource definition: " + definition.key());
                }
            }
        }
        Map<DatasourceKey, DatasourceDefinition> snapshot = Collections.unmodifiableMap(merged);
        definitions.set(snapshot);
        return snapshot;
    }

    /** @return 当前不可变定义快照 */
    public Map<DatasourceKey, DatasourceDefinition> snapshot() {
        return definitions.get();
    }

    /** @return primary 定义；不存在或重复时为空或抛错 */
    public Optional<DatasourceDefinition> primary() {
        List<DatasourceDefinition> primary = definitions.get().values().stream()
                .filter(DatasourceDefinition::primary).toList();
        if (primary.size() > 1) {
            throw new IllegalStateException("multiple primary datasource definitions");
        }
        return primary.stream().findFirst();
    }
}
