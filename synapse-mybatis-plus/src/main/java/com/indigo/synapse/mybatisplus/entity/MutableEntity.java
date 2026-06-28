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
 * 包含修改时间和修改人字段的 MyBatis-Plus 实体基类。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class MutableEntity extends CreatedEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}
