package com.indigo.synapse.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * @author 史偕成
 * @date 2026/06/28 10:11
 **/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class MutableEntity<T> extends CreatedEntity<T> {

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}
