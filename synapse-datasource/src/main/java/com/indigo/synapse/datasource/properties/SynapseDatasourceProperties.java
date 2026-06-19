package com.indigo.synapse.datasource.properties;

import com.indigo.synapse.datasource.loadbalance.LoadBalanceStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Synapse 数据源治理配置项。
 *
 * <p>该配置只描述数据源治理约定、检测、健康检查、负载均衡、故障转移和路由决策行为，
 * 不提供 ORM 配置、SQL 自动路由或业务数据源切换 API。</p>
 */
@ConfigurationProperties(prefix = "synapse.datasource")
public class SynapseDatasourceProperties {

    /**
     * 是否启用 Synapse 数据源治理能力。
     */
    private boolean enabled = true;

    /**
     * 数据源命名和接入约定。
     */
    private final Convention convention = new Convention();

    /**
     * 数据库类型识别配置。
     */
    private final Detection detection = new Detection();

    /**
     * 数据源健康检查配置。
     */
    private final Health health = new Health();

    /**
     * 数据源连接安全检测配置。
     */
    private final Safety safety = new Safety();

    /**
     * 读库负载均衡配置。
     */
    private final LoadBalance loadBalance = new LoadBalance();

    /**
     * 故障转移和恢复配置。
     */
    private final Failover failover = new Failover();

    /**
     * 路由决策配置。当前只生成路由决策，不启用 SQL 自动路由。
     */
    private final Router router = new Router();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Convention getConvention() {
        return convention;
    }

    public Detection getDetection() {
        return detection;
    }

    public Health getHealth() {
        return health;
    }

    public Safety getSafety() {
        return safety;
    }

    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public Failover getFailover() {
        return failover;
    }

    public Router getRouter() {
        return router;
    }

    public static class Convention {
        /**
         * dynamic-datasource 的 primary 必须使用的名称。
         */
        private String requiredPrimary = "master";

        /**
         * 是否要求 dynamic-datasource 开启 strict 模式。
         */
        private boolean requireStrict = true;

        /**
         * 主库数据源名称。
         */
        private String masterName = "master";

        /**
         * 读库数据源组名前缀。
         */
        private String slaveGroup = "slave";

        /**
         * 报表库数据源组名前缀。
         */
        private String reportGroup = "report";

        /**
         * 归档库数据源组名前缀。
         */
        private String archiveGroup = "archive";

        /**
         * 外部只读库数据源组名前缀。
         */
        private String externalGroup = "external";

        public String getRequiredPrimary() {
            return requiredPrimary;
        }

        public void setRequiredPrimary(String requiredPrimary) {
            this.requiredPrimary = requiredPrimary;
        }

        public boolean isRequireStrict() {
            return requireStrict;
        }

        public void setRequireStrict(boolean requireStrict) {
            this.requireStrict = requireStrict;
        }

        public String getMasterName() {
            return masterName;
        }

        public void setMasterName(String masterName) {
            this.masterName = masterName;
        }

        public String getSlaveGroup() {
            return slaveGroup;
        }

        public void setSlaveGroup(String slaveGroup) {
            this.slaveGroup = slaveGroup;
        }

        public String getReportGroup() {
            return reportGroup;
        }

        public void setReportGroup(String reportGroup) {
            this.reportGroup = reportGroup;
        }

        public String getArchiveGroup() {
            return archiveGroup;
        }

        public void setArchiveGroup(String archiveGroup) {
            this.archiveGroup = archiveGroup;
        }

        public String getExternalGroup() {
            return externalGroup;
        }

        public void setExternalGroup(String externalGroup) {
            this.externalGroup = externalGroup;
        }
    }

    public static class Detection {
        /**
         * 是否启用数据库类型识别。
         */
        private boolean enabled = true;

        /**
         * 数据库类型无法识别时是否失败。
         */
        private boolean failOnUnknown = false;

        /**
         * 存在显式数据库类型配置时是否优先使用显式配置。
         */
        private boolean preferExplicit = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isFailOnUnknown() {
            return failOnUnknown;
        }

        public void setFailOnUnknown(boolean failOnUnknown) {
            this.failOnUnknown = failOnUnknown;
        }

        public boolean isPreferExplicit() {
            return preferExplicit;
        }

        public void setPreferExplicit(boolean preferExplicit) {
            this.preferExplicit = preferExplicit;
        }
    }

    public static class Health {
        /**
         * 是否启用数据源健康检查。
         */
        private boolean enabled = true;

        /**
         * 健康检查初始延迟。
         */
        private Duration initialDelay = Duration.ofSeconds(10);

        /**
         * 健康检查间隔。
         */
        private Duration interval = Duration.ofSeconds(30);

        /**
         * 单次健康检查超时时间。
         */
        private Duration timeout = Duration.ofSeconds(2);

        /**
         * 连续失败达到该阈值后标记为 DOWN。
         */
        private int failureThreshold = 3;

        /**
         * 故障后连续成功达到该阈值后恢复为 UP。
         */
        private int recoveryThreshold = 2;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
        }

        public Duration getInterval() {
            return interval;
        }

        public void setInterval(Duration interval) {
            this.interval = interval;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public int getFailureThreshold() {
            return failureThreshold;
        }

        public void setFailureThreshold(int failureThreshold) {
            this.failureThreshold = failureThreshold;
        }

        public int getRecoveryThreshold() {
            return recoveryThreshold;
        }

        public void setRecoveryThreshold(int recoveryThreshold) {
            this.recoveryThreshold = recoveryThreshold;
        }
    }

    public static class Safety {
        /**
         * 是否启用数据源连接安全检测。
         */
        private boolean enabled = true;

        /**
         * 是否在启动阶段执行安全检测。
         */
        private boolean checkOnStartup = true;

        /**
         * 是否检查读库只读角色。
         */
        private boolean checkReadonlyRole = false;

        /**
         * 主库不可用时是否让启动或检查失败。
         */
        private boolean failOnMasterUnavailable = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isCheckOnStartup() {
            return checkOnStartup;
        }

        public void setCheckOnStartup(boolean checkOnStartup) {
            this.checkOnStartup = checkOnStartup;
        }

        public boolean isCheckReadonlyRole() {
            return checkReadonlyRole;
        }

        public void setCheckReadonlyRole(boolean checkReadonlyRole) {
            this.checkReadonlyRole = checkReadonlyRole;
        }

        public boolean isFailOnMasterUnavailable() {
            return failOnMasterUnavailable;
        }

        public void setFailOnMasterUnavailable(boolean failOnMasterUnavailable) {
            this.failOnMasterUnavailable = failOnMasterUnavailable;
        }
    }

    public static class LoadBalance {
        /**
         * 是否启用读库负载均衡选择器。
         */
        private boolean enabled = true;

        /**
         * 默认读库负载均衡策略。
         */
        private LoadBalanceStrategy defaultStrategy = LoadBalanceStrategy.ROUND_ROBIN;

        /**
         * 是否优先过滤不可用数据源。
         */
        private boolean healthFirst = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public LoadBalanceStrategy getDefaultStrategy() {
            return defaultStrategy;
        }

        public void setDefaultStrategy(LoadBalanceStrategy defaultStrategy) {
            this.defaultStrategy = defaultStrategy;
        }

        public boolean isHealthFirst() {
            return healthFirst;
        }

        public void setHealthFirst(boolean healthFirst) {
            this.healthFirst = healthFirst;
        }
    }

    public static class Failover {
        /**
         * 是否启用故障转移策略。
         */
        private boolean enabled = true;

        /**
         * 读库故障时是否从候选读库中排除。
         */
        private boolean excludeDownReadDatasource = true;

        /**
         * 所有读库不可用时是否回退到主库。
         */
        private boolean readFallbackToMaster = true;

        /**
         * 主库不可用时是否快速失败。
         */
        private boolean failFastWhenMasterDown = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isExcludeDownReadDatasource() {
            return excludeDownReadDatasource;
        }

        public void setExcludeDownReadDatasource(boolean excludeDownReadDatasource) {
            this.excludeDownReadDatasource = excludeDownReadDatasource;
        }

        public boolean isReadFallbackToMaster() {
            return readFallbackToMaster;
        }

        public void setReadFallbackToMaster(boolean readFallbackToMaster) {
            this.readFallbackToMaster = readFallbackToMaster;
        }

        public boolean isFailFastWhenMasterDown() {
            return failFastWhenMasterDown;
        }

        public void setFailFastWhenMasterDown(boolean failFastWhenMasterDown) {
            this.failFastWhenMasterDown = failFastWhenMasterDown;
        }
    }

    public static class Router {
        /**
         * 是否启用数据源路由决策器。
         */
        private boolean enabled = false;

        /**
         * 是否启用 SQL 自动读写路由。当前阶段固定建议保持关闭。
         */
        private boolean sqlAutoRouting = false;

        /**
         * 事务中读请求是否强制路由到主库。
         */
        private boolean forceMasterInTransaction = true;

        /**
         * 写操作后的读请求是否强制路由到主库。
         */
        private boolean forceMasterAfterWrite = true;

        /**
         * 锁查询是否强制路由到主库。
         */
        private boolean forceMasterForLockQuery = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isSqlAutoRouting() {
            return sqlAutoRouting;
        }

        public void setSqlAutoRouting(boolean sqlAutoRouting) {
            this.sqlAutoRouting = sqlAutoRouting;
        }

        public boolean isForceMasterInTransaction() {
            return forceMasterInTransaction;
        }

        public void setForceMasterInTransaction(boolean forceMasterInTransaction) {
            this.forceMasterInTransaction = forceMasterInTransaction;
        }

        public boolean isForceMasterAfterWrite() {
            return forceMasterAfterWrite;
        }

        public void setForceMasterAfterWrite(boolean forceMasterAfterWrite) {
            this.forceMasterAfterWrite = forceMasterAfterWrite;
        }

        public boolean isForceMasterForLockQuery() {
            return forceMasterForLockQuery;
        }

        public void setForceMasterForLockQuery(boolean forceMasterForLockQuery) {
            this.forceMasterForLockQuery = forceMasterForLockQuery;
        }
    }
}
