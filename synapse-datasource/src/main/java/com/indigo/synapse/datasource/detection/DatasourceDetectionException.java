package com.indigo.synapse.datasource.detection;

/**
 * 数据库类型识别失败异常。
 */
public class DatasourceDetectionException extends RuntimeException {

    public DatasourceDetectionException(String message) {
        super(message);
    }
}
