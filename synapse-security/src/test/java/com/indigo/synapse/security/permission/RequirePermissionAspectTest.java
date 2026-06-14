package com.indigo.synapse.security.permission;

import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.exception.SynapseAccessDeniedException;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirePermissionAspectTest {

    @Test
    void shouldRequireMethodPermissionAndProceed() throws Throwable {
        RecordingPermissionChecker permissionChecker = new RecordingPermissionChecker();
        RequirePermissionAspect aspect = new RequirePermissionAspect(permissionChecker);
        TestService target = new TestService();

        Object result = aspect.invoke(invocation(target, "create"));

        assertEquals("created", result);
        assertEquals(List.of("system:user:create"), permissionChecker.required);
        assertTrue(target.created);
    }

    @Test
    void shouldNotProceedWhenPermissionCheckerThrows() throws Throwable {
        RecordingPermissionChecker permissionChecker = new RecordingPermissionChecker();
        permissionChecker.throwAccessDenied = true;
        RequirePermissionAspect aspect = new RequirePermissionAspect(permissionChecker);
        TestService target = new TestService();

        assertThrows(SynapseAccessDeniedException.class, () -> aspect.invoke(invocation(target, "create")));

        assertEquals(List.of("system:user:create"), permissionChecker.required);
        assertFalse(target.created);
    }

    @Test
    void shouldProceedWithoutPermissionCheckWhenAnnotationMissing() throws Throwable {
        RecordingPermissionChecker permissionChecker = new RecordingPermissionChecker();
        RequirePermissionAspect aspect = new RequirePermissionAspect(permissionChecker);
        TestService target = new TestService();

        Object result = aspect.invoke(invocation(target, "open"));

        assertEquals("open", result);
        assertTrue(permissionChecker.required.isEmpty());
    }

    @Test
    void shouldSupportClassLevelPermission() throws Throwable {
        RecordingPermissionChecker permissionChecker = new RecordingPermissionChecker();
        RequirePermissionAspect aspect = new RequirePermissionAspect(permissionChecker);
        ClassLevelService target = new ClassLevelService();

        aspect.invoke(invocation(target, "list"));

        assertEquals(List.of("system:user"), permissionChecker.required);
    }

    @Test
    void shouldPreferMethodLevelPermissionOverClassLevelPermission() throws Throwable {
        RecordingPermissionChecker permissionChecker = new RecordingPermissionChecker();
        RequirePermissionAspect aspect = new RequirePermissionAspect(permissionChecker);
        ClassLevelService target = new ClassLevelService();

        aspect.invoke(invocation(target, "create"));

        assertEquals(List.of("system:user:create"), permissionChecker.required);
    }

    @Test
    void shouldDelegateBlankPermissionToPermissionChecker() throws Throwable {
        RecordingPermissionChecker permissionChecker = new RecordingPermissionChecker();
        permissionChecker.throwIllegalArgument = true;
        RequirePermissionAspect aspect = new RequirePermissionAspect(permissionChecker);
        TestService target = new TestService();

        assertThrows(IllegalArgumentException.class, () -> aspect.invoke(invocation(target, "blank")));

        assertEquals(List.of(""), permissionChecker.required);
        assertFalse(target.blank);
    }

    @Test
    void shouldMatchAnnotatedMethodOrClassOnly() throws NoSuchMethodException {
        RequirePermissionAspect aspect = new RequirePermissionAspect(new RecordingPermissionChecker());

        assertTrue(aspect.matches(TestService.class.getDeclaredMethod("create"), TestService.class));
        assertTrue(aspect.matches(ClassLevelService.class.getDeclaredMethod("list"), ClassLevelService.class));
        assertFalse(aspect.matches(TestService.class.getDeclaredMethod("open"), TestService.class));
    }

    private static MethodInvocation invocation(Object target, String methodName) throws NoSuchMethodException {
        Method method = target.getClass().getDeclaredMethod(methodName);
        return new SimpleMethodInvocation(target, method);
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

    private static final class RecordingPermissionChecker implements PermissionChecker {

        private final List<String> required = new ArrayList<>();
        private boolean throwAccessDenied;
        private boolean throwIllegalArgument;

        @Override
        public void require(String permission) {
            required.add(permission);
            if (throwIllegalArgument) {
                throw new IllegalArgumentException("permission must not be blank");
            }
            if (throwAccessDenied) {
                throw new SynapseAccessDeniedException();
            }
        }

        @Override
        public boolean has(String permission) {
            return false;
        }

        @Override
        public AuthenticatedUser requireUser() {
            throw new UnsupportedOperationException("not used");
        }
    }

    private static final class TestService {

        private boolean created;
        private boolean blank;

        @RequirePermission("system:user:create")
        String create() {
            created = true;
            return "created";
        }

        @RequirePermission("")
        String blank() {
            blank = true;
            return "blank";
        }

        String open() {
            return "open";
        }
    }

    @RequirePermission("system:user")
    private static final class ClassLevelService {

        void list() {
        }

        @RequirePermission("system:user:create")
        void create() {
        }
    }
}
