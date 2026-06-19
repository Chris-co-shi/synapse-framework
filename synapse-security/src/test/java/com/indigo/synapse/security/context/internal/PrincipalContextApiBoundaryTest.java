package com.indigo.synapse.security.context.internal;

import com.indigo.synapse.security.context.CurrentPrincipalContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrincipalContextApiBoundaryTest {

    @Test
    void shouldExposeOnlyReadOperationsFromCurrentPrincipalContext() {
        Set<String> publicStaticMethods =
                Arrays.stream(CurrentPrincipalContext.class.getDeclaredMethods())
                        .filter(method ->
                                Modifier.isPublic(method.getModifiers()))
                        .filter(method ->
                                Modifier.isStatic(method.getModifiers()))
                        .map(Method::getName)
                        .collect(Collectors.toUnmodifiableSet());

        assertEquals(
                Set.of(
                        "currentPrincipal",
                        "currentUser",
                        "currentClient"
                ),
                publicStaticMethods
        );
    }

    @Test
    void shouldNotExposePrincipalContextStateMutation() throws Exception {
        Method setPrincipal =
                PrincipalContextState.class.getDeclaredMethod(
                        "setPrincipal",
                        com.indigo.synapse.security.context
                                .AuthenticatedPrincipal.class
                );

        assertFalse(
                Modifier.isPublic(setPrincipal.getModifiers())
        );
        assertFalse(
                Modifier.isProtected(setPrincipal.getModifiers())
        );
    }

    @Test
    void shouldNotAllowApplicationCodeToConstructScope() {
        boolean hasPublicConstructor =
                Arrays.stream(
                                PrincipalContextScope.class
                                        .getDeclaredConstructors()
                        )
                        .anyMatch(constructor ->
                                Modifier.isPublic(
                                        constructor.getModifiers()
                                )
                        );

        assertFalse(hasPublicConstructor);
    }

    @Test
    void shouldExposeOnlyOperationContextConversionFromAdapter() {
        assertTrue(
                Modifier.isPublic(
                        SecurityOperationContextAdapter.class
                                .getModifiers()
                )
        );

        assertTrue(
                Modifier.isFinal(
                        SecurityOperationContextAdapter.class
                                .getModifiers()
                )
        );

        Set<String> publicStaticMethods =
                Arrays.stream(
                                SecurityOperationContextAdapter.class
                                        .getDeclaredMethods()
                        )
                        .filter(method ->
                                Modifier.isPublic(method.getModifiers()))
                        .filter(method ->
                                Modifier.isStatic(method.getModifiers()))
                        .map(Method::getName)
                        .collect(Collectors.toUnmodifiableSet());

        assertEquals(
                Set.of("toOperationContext"),
                publicStaticMethods
        );
    }

    @Test
    void shouldKeepOperationContextAdapterNonInstantiable() {
        boolean allConstructorsPrivate =
                Arrays.stream(
                                SecurityOperationContextAdapter.class
                                        .getDeclaredConstructors()
                        )
                        .allMatch(constructor ->
                                Modifier.isPrivate(
                                        constructor.getModifiers()
                                )
                        );

        assertTrue(allConstructorsPrivate);
    }

    @Test
    void shouldKeepBinderNonInstantiable() {
        boolean allConstructorsPrivate =
                Arrays.stream(
                                PrincipalContextBinder.class
                                        .getDeclaredConstructors()
                        )
                        .allMatch(constructor ->
                                Modifier.isPrivate(
                                        constructor.getModifiers()
                                )
                        );

        assertTrue(allConstructorsPrivate);
    }
}
