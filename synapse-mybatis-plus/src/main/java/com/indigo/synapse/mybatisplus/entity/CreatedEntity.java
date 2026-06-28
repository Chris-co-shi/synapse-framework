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
 * @author 史偕成
 * @date 2026/06/28 10:09
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class CreatedEntity<T> extends IdEntity<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
}
