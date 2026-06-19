package com.indigo.synapse.oauth2.resource.core;

import com.indigo.synapse.oauth2.core.jwt.JwtClaimValues;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;

import java.util.ArrayList;
import java.util.List;

/** 将 scope、roles 和 permissions 按稳定顺序映射为 Spring Security authority 字符串。 */
public final class SynapseAuthorityClaimMapper {

    /**
     * 生成去重后的 authority 名称。
     *
     * @param claims JWT claim 读取端口
     * @return 按 scope、role、permission 排序的不可变列表
     */
    public List<String> map(JwtClaimAccessor claims) {
        List<String> authorities = new ArrayList<>();
        JwtClaimValues.strings(claims, SynapseJwtClaimNames.SCOPE)
                .forEach(value -> authorities.add(prefix("SCOPE_", value)));
        JwtClaimValues.strings(claims, SynapseJwtClaimNames.ROLES)
                .forEach(value -> authorities.add(prefix("ROLE_", value)));
        JwtClaimValues.strings(claims, SynapseJwtClaimNames.PERMISSIONS)
                .forEach(value -> authorities.add(prefix("PERM_", value)));
        return List.copyOf(authorities);
    }

    private static String prefix(String prefix, String value) {
        return value.startsWith(prefix) ? value : prefix + value;
    }
}
