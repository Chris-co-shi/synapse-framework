package com.indigo.synapse.security.permission;

import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.context.SecurityContext;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PermissionEntryPointContractTest {

    private final DefaultPermissionChecker permissionChecker = new DefaultPermissionChecker();
    private final RequirePermissionAspect aspect = new RequirePermissionAspect(permissionChecker);
    private final SecuredService securedService = new SecuredService();

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
        OperationContextHolder.clear();
    }

    @Test
    void shouldAllowSamePermissionThroughExplicitAndAnnotationEntryPoints() throws Throwable {
        SecurityContext.set(user("sample:read"));

        permissionChecker.require("sample:read");
        Object result = aspect.invoke(invocation(securedService, "read"));

        assertEquals("read", result);
    }

    @Test
    void shouldReturnSameAccessDeniedCodeThroughExplicitAndAnnotationEntryPoints() throws NoSuchMethodException {
        SecurityContext.set(user("sample:write"));

        SynapseAccessDeniedException explicitException = assertThrows(
                SynapseAccessDeniedException.class,
                () -> permissionChecker.require("sample:read")
        );
        SynapseAccessDeniedException annotationException = assertThrows(
                SynapseAccessDeniedException.class,
                () -> aspect.invoke(invocation(securedService, "read"))
        );

        assertEquals(explicitException.errorCode(), annotationException.errorCode());
    }

    @Test
    void shouldReturnSameAuthenticationCodeThroughExplicitAndAnnotationEntryPoints() throws NoSuchMethodException {
        SynapseAuthenticationException explicitException = assertThrows(
                SynapseAuthenticationException.class,
                () -> permissionChecker.require("sample:read")
        );
        SynapseAuthenticationException annotationException = assertThrows(
                SynapseAuthenticationException.class,
                () -> aspect.invoke(invocation(securedService, "read"))
        );

        assertEquals(explicitException.errorCode(), annotationException.errorCode());
    }

    private static MethodInvocation invocation(Object target, String methodName) throws NoSuchMethodException {
        Method method = target.getClass().getDeclaredMethod(methodName);
        return new SimpleMethodInvocation(target, method);
    }

    private static AuthenticatedUser user(String permission) {
        return new AuthenticatedUser(
                "user-1",
                "admin",
                "tenant-a",
                Set.of("ADMIN"),
                Set.of(permission)
        );
    }

    private static final class SimpleMethodInvocation implements MethodInvocation {

        private final Object target;
        private final Method method;

        private SimpleMethodInvocation(Object target, Method method) {
            this.target = target;
            this.method = method;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Object[] getArguments() {
            return new Object[0];
        }

        @Override
        public Object proceed() throws Throwable {
            return method.invoke(target);
        }

        @Override
        public Object getThis() {
            return target;
        }

        @Override
        public AccessibleObject getStaticPart() {
            return method;
        }
    }

    private static final class SecuredService {

        @RequirePermission("sample:read")
        String read() {
            return "read";
        }
    }
}
