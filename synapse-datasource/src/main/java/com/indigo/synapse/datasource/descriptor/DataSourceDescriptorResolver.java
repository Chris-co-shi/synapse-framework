package com.indigo.synapse.datasource.descriptor;

import com.indigo.synapse.datasource.detection.CompositeDbTypeDetector;
import com.indigo.synapse.datasource.detection.DatasourceDetectionException;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

/**
 * 数据源描述符解析器。
 *
 * <p>解析器根据数据源名称、primary 名称、命名约定和数据库类型检测结果创建描述符。
 * 输出属性只允许非敏感治理元数据，不携带密码、密钥或用户名。</p>
 */
public class DataSourceDescriptorResolver {

    private final SynapseDatasourceProperties properties;
    private final CompositeDbTypeDetector dbTypeDetector;

    public DataSourceDescriptorResolver(
            SynapseDatasourceProperties properties,
            CompositeDbTypeDetector dbTypeDetector
    ) {
        this.properties = properties;
        this.dbTypeDetector = dbTypeDetector;
    }

    /**
     * 解析单个数据源描述符。
     *
     * @param name 数据源名称
     * @param dataSource 数据源对象；只用于数据库类型 metadata 检测，不会被关闭或持有
     * @param jdbcUrl 可选 JDBC URL；只参与类型识别，不写入描述符属性
     * @param primaryName dynamic-datasource 显式 primary 名称
     * @return 数据源描述符
     */
    public DataSourceDescriptor resolve(
            String name,
            DataSource dataSource,
            Optional<String> jdbcUrl,
            Optional<String> primaryName
    ) {
        boolean primary = primaryName.map(name::equals).orElse(false);
        DataSourceRole role = resolveRole(name, primary);
        String group = resolveGroup(name, role);
        SynapseDbType dbType = properties.getDetection().isEnabled()
                ? dbTypeDetector.detectOrUnknown(name, dataSource, jdbcUrl.orElse(null))
                : SynapseDbType.UNKNOWN;
        if (dbType == SynapseDbType.UNKNOWN && properties.getDetection().isFailOnUnknown()) {
            throw new DatasourceDetectionException("Cannot detect database type for datasource " + name);
        }
        return new DataSourceDescriptor(
                name,
                group,
                role,
                dbType,
                primary,
                role != DataSourceRole.MASTER,
                true,
                Map.of()
        );
    }

    private DataSourceRole resolveRole(String name, boolean primary) {
        SynapseDatasourceProperties.Convention convention = properties.getConvention();
        if (primary || convention.getMasterName().equals(name)) {
            return DataSourceRole.MASTER;
        }
        if (matchesGroup(name, convention.getSlaveGroup())) {
            return DataSourceRole.SLAVE;
        }
        if (matchesGroup(name, convention.getReportGroup())) {
            return DataSourceRole.REPORT;
        }
        if (matchesGroup(name, convention.getArchiveGroup())) {
            return DataSourceRole.ARCHIVE;
        }
        if (matchesGroup(name, convention.getExternalGroup())) {
            return DataSourceRole.EXTERNAL;
        }
        return DataSourceRole.UNKNOWN;
    }

    private String resolveGroup(String name, DataSourceRole role) {
        SynapseDatasourceProperties.Convention convention = properties.getConvention();
        return switch (role) {
            case MASTER -> convention.getMasterName();
            case SLAVE -> convention.getSlaveGroup();
            case REPORT -> convention.getReportGroup();
            case ARCHIVE -> convention.getArchiveGroup();
            case EXTERNAL -> convention.getExternalGroup();
            case UNKNOWN -> name;
        };
    }

    private boolean matchesGroup(String name, String group) {
        return name.equals(group) || name.startsWith(group + "_");
    }
}
