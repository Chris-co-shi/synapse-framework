package com.indigo.synapse.audit.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.indigo.synapse.data.entity.BaseLogEntity;

@TableName("audit_log")
public class AuditLogEntity extends BaseLogEntity {

    private String action;
    private String subjectType;
    private String subjectId;
    private String subjectTenantId;
    private String targetType;
    private String targetId;
    private String outcome;
    private String traceId;
    private String message;
    private String attributesJson;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectTenantId() {
        return subjectTenantId;
    }

    public void setSubjectTenantId(String subjectTenantId) {
        this.subjectTenantId = subjectTenantId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAttributesJson() {
        return attributesJson;
    }

    public void setAttributesJson(String attributesJson) {
        this.attributesJson = attributesJson;
    }
}
