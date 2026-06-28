package com.indigo.synapse.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

/**
 * @author 史偕成
 * @date 2026/06/28 10:12
 **/
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class VersionedEntity  extends  MutableEntity{

    @Version
    private Integer revision;
}
