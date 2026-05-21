package com.indigo.synapse.iam.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.indigo.synapse.data.entity.BaseEntity;

@TableName("iam_user")
public class IamUserEntity extends BaseEntity {

    @TableField("username")
    private String username;

    @TableField("display_name")
    private String displayName;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("status")
    private String status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
