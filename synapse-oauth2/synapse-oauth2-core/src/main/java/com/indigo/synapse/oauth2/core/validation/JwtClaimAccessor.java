package com.indigo.synapse.oauth2.core.validation;

import java.util.Collection;
import java.util.Optional;

/**
 * JWT claim 读取端口。
 */
public interface JwtClaimAccessor {

    Optional<String> string(String name);

    Collection<String> strings(String name);
}
