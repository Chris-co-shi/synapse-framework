package com.indigo.synapse.data.field;

/**
 * Synapse 标准数据字段名。
 */
public final class DataFieldNames {

    public static final String ID = "id";

    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String CREATED_BY = "createdBy";
    public static final String UPDATED_BY = "updatedBy";

    public static final String DELETED = "deleted";
    public static final String REVISION = "revision";

    /**
     * 旧版消费方自定义乐观锁字段名。
     *
     * @deprecated Framework 标准实体基类使用 {@link #REVISION}。
     */
    @Deprecated(forRemoval = false, since = "0.1.0")
    public static final String VERSION = "version";

    private DataFieldNames() {
    }
}
