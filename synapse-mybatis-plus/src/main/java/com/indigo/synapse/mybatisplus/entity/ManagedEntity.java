package com.indigo.synapse.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author 史偕成
 * @date 2026/06/28 10:13
 **/
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class ManagedEntity<T> extends VersionedEntity<T> {

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
