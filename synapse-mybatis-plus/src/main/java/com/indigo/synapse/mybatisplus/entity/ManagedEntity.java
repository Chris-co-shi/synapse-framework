package com.indigo.synapse.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.*;

import java.io.Serial;

/**
 * 同时包含乐观锁和逻辑删除字段的 MyBatis-Plus 管理型实体基类。
 *
 * <p>消费方应根据实体生命周期显式选择该基类，不能让所有实体机械继承。</p>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class ManagedEntity extends VersionedEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
