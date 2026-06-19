package com.indigo.synapse.datasource.safety;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthSnapshot;
import com.indigo.synapse.datasource.health.DataSourceHealthStatus;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 数据源安全检查器。
 *
 * <p>该类属于 `synapse-datasource` 安全治理边界，主要调用方是
 * {@code DatasourceGovernanceLifecycle} 和测试。它根据运行时 inventory、描述符注册表、健康注册表和
 * Synapse 配置检查启动安全条件。它不关闭 DataSource，不执行动态数据源切换，不做主库晋升，也不读取敏感连接信息。</p>
 *
 * <p>实例无状态、线程安全。所有检查方法都是幂等的纯判断：成功返回 safe report，失败返回包含稳定 violation code
 * 的 report；{@link #assertSafe(DataSourceSafetyReport)} 才会把失败报告转为异常。</p>
 */
public class DataSourceSafetyChecker {

    private final SynapseDatasourceProperties properties;

    public DataSourceSafetyChecker(SynapseDatasourceProperties properties) {
        this.properties = properties;
    }

    /**
     * 检查显式 primary 名称是否存在且符合 Synapse 约定。
     *
     * @param primary dynamic-datasource 显式 primary 名称
     * @return 安全检查报告
     */
    public DataSourceSafetyReport checkPrimary(String primary) {
        if (primary == null || primary.isBlank()) {
            return DataSourceSafetyReport.violation(
                    "dynamic-datasource",
                    "Primary datasource is missing.",
                    DataSourceSafetyViolationCode.PRIMARY_MISSING
            );
        }
        String required = properties.getConvention().getMasterName();
        boolean safe = required.equals(primary);
        return safe
                ? DataSourceSafetyReport.safe(primary, "Primary datasource is valid.")
                : DataSourceSafetyReport.violation(
                        primary,
                        "Primary datasource must be " + required,
                        DataSourceSafetyViolationCode.PRIMARY_NAME_MISMATCH
                );
    }

    /**
     * 检查 dynamic-datasource strict 模式。
     *
     * @param strict 当前 strict 配置
     * @return 安全检查报告
     */
    public DataSourceSafetyReport checkStrict(boolean strict) {
        boolean required = properties.getConvention().isRequireStrict();
        boolean safe = !required || strict;
        return safe
                ? DataSourceSafetyReport.safe("dynamic-datasource", "Dynamic datasource strict mode is valid.")
                : DataSourceSafetyReport.violation(
                        "dynamic-datasource",
                        "spring.datasource.dynamic.strict must be true",
                        DataSourceSafetyViolationCode.STRICT_MODE_REQUIRED
                );
    }

    /**
     * 综合检查 primary 和 master 描述符。
     *
     * @param primaryName runtime inventory 提供的 primary 名称
     * @param dataSources runtime inventory 当前数据源快照
     * @param descriptorRegistry 描述符注册表
     * @return 安全检查报告
     */
    public DataSourceSafetyReport checkPrimaryDescriptor(
            Optional<String> primaryName,
            Map<String, DataSource> dataSources,
            DataSourceDescriptorRegistry descriptorRegistry
    ) {
        DataSourceSafetyReport primaryReport = checkPrimary(primaryName.orElse(null));
        if (!primaryReport.safe()) {
            return primaryReport;
        }
        String primary = primaryName.orElseThrow();
        if (dataSources == null || !dataSources.containsKey(primary)) {
            return DataSourceSafetyReport.violation(
                    primary,
                    "Primary datasource does not exist in runtime inventory.",
                    DataSourceSafetyViolationCode.MASTER_DATASOURCE_MISSING
            );
        }
        List<DataSourceDescriptor> primaries = descriptorRegistry.findPrimaries();
        if (primaries.size() > 1) {
            return DataSourceSafetyReport.violation(
                    primary,
                    "Multiple primary datasource descriptors detected.",
                    DataSourceSafetyViolationCode.MULTIPLE_PRIMARY_DATASOURCES
            );
        }
        if (primaries.isEmpty()) {
            return DataSourceSafetyReport.violation(
                    primary,
                    "Primary datasource descriptor is missing.",
                    DataSourceSafetyViolationCode.PRIMARY_MISSING
            );
        }
        DataSourceDescriptor descriptor = primaries.getFirst();
        if (descriptor.role() != DataSourceRole.MASTER) {
            return DataSourceSafetyReport.violation(
                    descriptor.name(),
                    "Primary datasource role must be MASTER.",
                    DataSourceSafetyViolationCode.PRIMARY_ROLE_MISMATCH
            );
        }
        return DataSourceSafetyReport.safe(primary, "Primary descriptor is valid.");
    }

    /**
     * 检查主库健康状态。
     *
     * @param masterDescriptor 唯一 primary 描述符
     * @param healthRegistry 健康状态注册表
     * @return 安全检查报告
     */
    public DataSourceSafetyReport checkMasterAvailable(
            DataSourceDescriptor masterDescriptor,
            DataSourceHealthRegistry healthRegistry
    ) {
        Optional<DataSourceHealthSnapshot> snapshot = healthRegistry.find(masterDescriptor.name());
        if (snapshot.isEmpty() || snapshot.get().status() != DataSourceHealthStatus.UP) {
            return DataSourceSafetyReport.violation(
                    masterDescriptor.name(),
                    "Master datasource is unavailable.",
                    DataSourceSafetyViolationCode.MASTER_UNAVAILABLE
            );
        }
        return DataSourceSafetyReport.safe(masterDescriptor.name(), "Master datasource is available.");
    }

    /**
     * 检查描述符中的数据库类型是否已知。
     *
     * @param descriptors 描述符集合
     * @return 安全检查报告
     */
    public DataSourceSafetyReport checkKnownDatabaseTypes(List<DataSourceDescriptor> descriptors) {
        return descriptors.stream()
                .filter(descriptor -> descriptor.dbType() == SynapseDbType.UNKNOWN)
                .findFirst()
                .map(descriptor -> DataSourceSafetyReport.violation(
                        descriptor.name(),
                        "Database type is UNKNOWN.",
                        DataSourceSafetyViolationCode.UNKNOWN_DATABASE_TYPE
                ))
                .orElseGet(() -> DataSourceSafetyReport.safe("datasource", "All database types are known."));
    }

    /**
     * 检查命名约定角色与数据库真实角色是否一致。
     *
     * <p>只有健康快照提供了真实角色时才执行比对；Oracle 等暂不支持角色检测的策略不会被误判。
     * MASTER 描述符必须检测为 MASTER，只读描述符必须检测为非 MASTER。</p>
     *
     * @param descriptors 描述符集合
     * @param healthRegistry 健康状态注册表
     * @return 安全检查报告
     */
    public DataSourceSafetyReport checkReadonlyRole(
            List<DataSourceDescriptor> descriptors,
            DataSourceHealthRegistry healthRegistry
    ) {
        for (DataSourceDescriptor descriptor : descriptors) {
            Optional<DataSourceHealthSnapshot> snapshot = healthRegistry.find(descriptor.name());
            if (snapshot.isEmpty() || snapshot.get().detectedRole() == null) {
                continue;
            }
            DataSourceRole detectedRole = snapshot.get().detectedRole();
            boolean mismatch = descriptor.role() == DataSourceRole.MASTER
                    ? detectedRole != DataSourceRole.MASTER
                    : descriptor.readonly() && detectedRole == DataSourceRole.MASTER;
            if (mismatch) {
                return DataSourceSafetyReport.violation(
                        descriptor.name(),
                        "Datasource declared role does not match detected database role.",
                        DataSourceSafetyViolationCode.READONLY_ROLE_MISMATCH
                );
            }
        }
        return DataSourceSafetyReport.safe("datasource", "Datasource detected roles are valid.");
    }

    /**
     * 将失败报告转换为异常。
     *
     * @param report 安全检查报告
     * @throws DatasourceSafetyException 当报告不安全时抛出
     */
    public void assertSafe(DataSourceSafetyReport report) {
        if (!report.safe()) {
            throw new DatasourceSafetyException(report);
        }
    }
}
