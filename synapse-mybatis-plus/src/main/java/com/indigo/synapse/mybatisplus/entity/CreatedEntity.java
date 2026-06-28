package com.indigo.synapse.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.Instant;

/**
 * 包含创建时间和创建人字段的 MyBatis-Plus 实体基类。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class CreatedEntity extends IdEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
}
