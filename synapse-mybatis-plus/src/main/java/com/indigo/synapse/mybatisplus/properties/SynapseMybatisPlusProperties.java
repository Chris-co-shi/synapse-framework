package com.indigo.synapse.mybatisplus.properties;

import com.baomidou.mybatisplus.annotation.DbType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Synapse MyBatis-Plus 工程增强配置项。
 *
 * <p>该配置控制 MyBatis-Plus 插件链、审计字段填充和分页适配行为，不承载 DataSource 治理、
 * 业务 Mapper 或业务 Entity 配置。</p>
 */
@ConfigurationProperties(prefix = "synapse.mybatis-plus")
@Validated
public class SynapseMybatisPlusProperties {

    /**
     * 是否启用 Synapse MyBatis-Plus 工程增强。
     */
    private boolean enabled = true;

    /**
     * 分页插件配置。
     */
    @Valid
    private final Pagination pagination = new Pagination();

    /**
     * 乐观锁插件配置。
     */
    @Valid
    private final OptimisticLock optimisticLock = new OptimisticLock();

    /**
     * 防全表更新删除插件配置。
     */
    @Valid
    private final BlockAttack blockAttack = new BlockAttack();

    /**
     * 非法 SQL 插件配置。
     */
    @Valid
    private final IllegalSql illegalSql = new IllegalSql();

    /**
     * 审计字段自动填充配置。
     */
    @Valid
    private final AuditFill auditFill = new AuditFill();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public OptimisticLock getOptimisticLock() {
        return optimisticLock;
    }

    public BlockAttack getBlockAttack() {
        return blockAttack;
    }

    public IllegalSql getIllegalSql() {
        return illegalSql;
    }

    public AuditFill getAuditFill() {
        return auditFill;
    }

    public static class Pagination {
        /**
         * 是否启用分页插件。
         */
        private boolean enabled = true;

        /**
         * MyBatis-Plus 数据库类型；为空时由 MyBatis-Plus 自行处理。
         */
        private DbType dbType;

        /**
         * 单页最大记录数。
         */
        @Positive
        private Long maxLimit = 500L;

        /**
         * 页码溢出时是否回到第一页。
         */
        private boolean overflow = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public DbType getDbType() {
            return dbType;
        }

        public void setDbType(DbType dbType) {
            this.dbType = dbType;
        }

        public Long getMaxLimit() {
            return maxLimit;
        }

        public void setMaxLimit(Long maxLimit) {
            this.maxLimit = maxLimit;
        }

        public boolean isOverflow() {
            return overflow;
        }

        public void setOverflow(boolean overflow) {
            this.overflow = overflow;
        }
    }

    public static class OptimisticLock {
        /**
         * 是否启用乐观锁插件。
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class BlockAttack {
        /**
         * 是否启用防全表更新删除插件。
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class IllegalSql {
        /**
         * 是否启用非法 SQL 插件。
         */
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class AuditFill {
        /**
         * 是否启用审计字段自动填充。
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
