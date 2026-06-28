package com.indigo.synapse.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author 史偕成
 * @date 2026/06/28 10:12
 **/
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public abstract class VersionedEntity<T>  extends  MutableEntity<T>{

    @Version
    private Integer revision;
}
