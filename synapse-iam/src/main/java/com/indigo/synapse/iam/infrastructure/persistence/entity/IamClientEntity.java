package com.indigo.synapse.iam.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.indigo.synapse.data.entity.BaseEntity;

@TableName("iam_client")
public class IamClientEntity extends BaseEntity {

    @TableField("client_id")
    private String clientId;

    @TableField("enabled")
    private Boolean enabled;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
