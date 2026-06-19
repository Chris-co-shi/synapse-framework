package com.indigo.synapse.datasource.routing;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.indigo.synapse.datasource.definition.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DatasourceRouteSelectorTest {

    @AfterEach
    void clear() {
        DynamicDataSourceContextHolder.clear();
    }

    @Test
    void shouldRespectScopeAnnotationResolverAndPrimaryPriority() throws Exception {
        DatasourceRegistry registry = new DatasourceRegistry(List.of(() -> List.of(
                DatasourceRegistryTest.definition("master", true))));
        registry.refresh();
        DatasourceRouteContext context = new DatasourceRouteContext(() -> false);
        DatasourceRouteResolver resolver = invocation -> java.util.Optional.of(new DatasourceKey("resolver"));
        DatasourceRouteSelector selector = new DatasourceRouteSelector(context, registry, List.of(resolver));
        Method annotated = Sample.class.getDeclaredMethod("annotated");
        Method plain = Sample.class.getDeclaredMethod("plain");

        assertThat(selector.select(new DatasourceRouteInvocation(annotated, new Sample(), null)).value())
                .isEqualTo("annotation");
        assertThat(selector.select(new DatasourceRouteInvocation(plain, new Sample(), null)).value())
                .isEqualTo("resolver");
        try (DatasourceRouteScope ignored = context.open(new DatasourceKey("scope"))) {
            assertThat(selector.select(new DatasourceRouteInvocation(annotated, new Sample(), null)).value())
                    .isEqualTo("scope");
        }
    }

    @Test
    void shouldUsePrimaryWhenResolversDoNotMatch() throws Exception {
        DatasourceRegistry registry = new DatasourceRegistry(List.of(() -> List.of(
                DatasourceRegistryTest.definition("master", true))));
        registry.refresh();
        DatasourceRouteSelector selector = new DatasourceRouteSelector(
                new DatasourceRouteContext(() -> false), registry, List.of(invocation -> java.util.Optional.empty()));

        assertThat(selector.select(new DatasourceRouteInvocation(
                Sample.class.getDeclaredMethod("plain"), new Sample(), null)).value()).isEqualTo("master");
    }

    static class Sample {
        @UseDatasource("annotation")
        void annotated() {
        }

        void plain() {
        }
    }
}
