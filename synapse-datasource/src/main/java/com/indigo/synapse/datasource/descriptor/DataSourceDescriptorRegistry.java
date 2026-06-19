package com.indigo.synapse.datasource.descriptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源描述符注册表。
 *
 * <p>该注册表保存治理层可见的数据源描述符快照。所有外部返回集合均为不可变副本，
 * 调用方不能通过返回值修改注册表内部状态。</p>
 */
public class DataSourceDescriptorRegistry {

    private final Map<String, DataSourceDescriptor> descriptors = new ConcurrentHashMap<>();

    /**
     * 注册或覆盖单个数据源描述符。
     *
     * @param descriptor 数据源描述符
     */
    public void register(DataSourceDescriptor descriptor) {
        descriptors.put(descriptor.name(), descriptor);
    }

    /**
     * 批量注册数据源描述符。
     *
     * @param descriptors 数据源描述符集合
     */
    public void registerAll(Collection<DataSourceDescriptor> descriptors) {
        descriptors.forEach(this::register);
    }

    /**
     * 按名称查找数据源描述符。
     *
     * @param name 数据源名称
     * @return 数据源描述符
     */
    public Optional<DataSourceDescriptor> find(String name) {
        return Optional.ofNullable(descriptors.get(name));
    }

    /**
     * 返回所有数据源描述符。
     *
     * @return 不可变描述符集合
     */
    public Collection<DataSourceDescriptor> findAll() {
        return List.copyOf(descriptors.values());
    }

    /**
     * 按组名查找数据源描述符。
     *
     * @param group 组名
     * @return 不可变描述符集合
     */
    public List<DataSourceDescriptor> findByGroup(String group) {
        return descriptors.values().stream()
                .filter(descriptor -> descriptor.group().equals(group))
                .toList();
    }

    /**
     * 按角色查找数据源描述符。
     *
     * @param role 数据源角色
     * @return 不可变描述符集合
     */
    public List<DataSourceDescriptor> findByRole(DataSourceRole role) {
        return descriptors.values().stream()
                .filter(descriptor -> descriptor.role() == role)
                .toList();
    }

    /**
     * 查找唯一 primary 数据源描述符。
     *
     * <p>如果注册表中不存在 primary 或存在多个 primary，本方法返回空，调用方必须通过
     * {@link #findPrimaries()} 区分缺失和重复。这样可以避免在多个 primary 时随机选择第一个导致路由错误。</p>
     *
     * @return 唯一 primary 描述符
     */
    public Optional<DataSourceDescriptor> findPrimary() {
        List<DataSourceDescriptor> primaries = findPrimaries();
        return primaries.size() == 1 ? Optional.of(primaries.getFirst()) : Optional.empty();
    }

    /**
     * 查找所有被标记为 primary 的描述符。
     *
     * @return 不可变 primary 描述符列表
     */
    public List<DataSourceDescriptor> findPrimaries() {
        return descriptors.values().stream()
                .filter(DataSourceDescriptor::primary)
                .toList();
    }

    /**
     * 移除指定数据源描述符。
     *
     * @param name 数据源名称
     * @return 被移除的描述符
     */
    public Optional<DataSourceDescriptor> remove(String name) {
        return Optional.ofNullable(descriptors.remove(name));
    }

    /**
     * 清空注册表。
     */
    public void clear() {
        descriptors.clear();
    }
}
