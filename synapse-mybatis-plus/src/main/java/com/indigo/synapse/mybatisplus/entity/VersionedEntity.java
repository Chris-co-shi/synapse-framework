package com.indigo.synapse.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.io.Serial;

/**
 * 包含技术乐观锁版本字段的 MyBatis-Plus 实体基类。
 *
 * <p>{@code revision} 只表达持久化并发控制，不应同时承担业务版本号含义。</p>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class VersionedEntity extends MutableEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Integer revision;
}
