package com.indigo.synapse.iam.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.indigo.synapse.data.entity.BaseEntity;

@TableName("iam_role_permission")
public class IamRolePermissionEntity extends BaseEntity {

    @TableField("role_id")
    private String roleId;

    @TableField("permission_id")
    private String permissionId;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }
}
