package com.indigo.synapse.iam.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.indigo.synapse.data.entity.BaseEntity;

@TableName("iam_role")
public class IamRoleEntity extends BaseEntity {

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("enabled")
    private Boolean enabled;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
