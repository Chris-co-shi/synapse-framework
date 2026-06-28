package com.indigo.synapse.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 使用字符串分布式主键的 MyBatis-Plus 持久化实体基类。
 *
 * <p>该类型只定义持久化实体的技术身份，不应被领域模型、API DTO 或事件契约继承。</p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class IdEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * MyBatis-Plus 自动分配的字符串主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
}
