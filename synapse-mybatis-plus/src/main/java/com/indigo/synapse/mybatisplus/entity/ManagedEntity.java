package com.indigo.synapse.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.*;

/**
 * @author 史偕成
 * @date 2026/06/28 10:13
 **/
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class ManagedEntity extends VersionedEntity {

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
