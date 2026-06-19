package com.indigo.synapse.oauth2.core.jwt;

/**
 * Synapse OAuth2 / JWT claim 名称契约。
 */
public final class SynapseJwtClaimNames {

    public static final String ISSUER = "iss";
    public static final String SUBJECT = "sub";
    public static final String AUDIENCE = "aud";
    public static final String EXPIRES_AT = "exp";
    public static final String ISSUED_AT = "iat";
    public static final String NOT_BEFORE = "nbf";
    public static final String TOKEN_ID = "jti";
    public static final String SESSION_ID = "sid";
    public static final String PREFERRED_USERNAME = "preferred_username";
    public static final String CLIENT_ID = "client_id";
    public static final String SCOPE = "scope";
    public static final String ROLES = "roles";
    public static final String PERMISSIONS = "permissions";
    public static final String TENANT_ID = "tenant_id";
    public static final String PRINCIPAL_TYPE = "principal_type";
    public static final String TOKEN_TYPE = "token_type";

    private SynapseJwtClaimNames() {
    }
}
