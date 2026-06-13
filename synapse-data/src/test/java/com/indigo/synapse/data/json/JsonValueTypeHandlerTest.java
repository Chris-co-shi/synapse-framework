package com.indigo.synapse.data.json;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JsonValueTypeHandlerTest {

    private final JsonValueTypeHandler typeHandler = new JsonValueTypeHandler();

    @Test
    void shouldSetJsonValueAsJdbcOther() throws Exception {
        CapturingInvocationHandler invocationHandler = new CapturingInvocationHandler();
        PreparedStatement preparedStatement = proxy(PreparedStatement.class, invocationHandler);

        typeHandler.setNonNullParameter(preparedStatement, 3, JsonValue.of("{\"enabled\":true}"), JdbcType.OTHER);

        assertEquals(3, invocationHandler.argument("setObject", 0));
        assertEquals("{\"enabled\":true}", invocationHandler.argument("setObject", 1));
        assertEquals(Types.OTHER, invocationHandler.argument("setObject", 2));
    }

    @Test
    void shouldReadJsonValueByColumnName() throws Exception {
        ResultSet resultSet = resultSetProxy("metadata", "{\"level\":1}");

        JsonValue result = typeHandler.getNullableResult(resultSet, "metadata");

        assertEquals(JsonValue.of("{\"level\":1}"), result);
    }

    @Test
    void shouldReadJsonValueByColumnIndex() throws Exception {
        ResultSet resultSet = resultSetProxy(2, "[1,2,3]");

        JsonValue result = typeHandler.getNullableResult(resultSet, 2);

        assertEquals(JsonValue.of("[1,2,3]"), result);
    }

    @Test
    void shouldReturnNullWhenDatabaseValueIsNull() throws Exception {
        ResultSet resultSet = resultSetProxy("metadata", null);

        assertNull(typeHandler.getNullableResult(resultSet, "metadata"));
    }

    @Test
    void shouldReadJsonValueFromCallableStatement() throws Exception {
        CallableStatement callableStatement = resultSetProxy(1, "{\"status\":\"OK\"}", CallableStatement.class);

        JsonValue result = typeHandler.getNullableResult(callableStatement, 1);

        assertEquals(JsonValue.of("{\"status\":\"OK\"}"), result);
    }

    private static <T> T proxy(Class<T> interfaceType, InvocationHandler invocationHandler) {
        return interfaceType.cast(Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                invocationHandler
        ));
    }

    private static ResultSet resultSetProxy(Object key, String value) {
        return resultSetProxy(key, value, ResultSet.class);
    }

    private static <T> T resultSetProxy(Object key, String value, Class<T> interfaceType) {
        return proxy(interfaceType, (proxy, method, args) -> {
            if ("getString".equals(method.getName()) && args != null && args.length == 1 && key.equals(args[0])) {
                return value;
            }
            if ("wasNull".equals(method.getName())) {
                return value == null;
            }
            throw new UnsupportedOperationException(method.getName() + " is not required for this test");
        });
    }

    private static final class CapturingInvocationHandler implements InvocationHandler {

        private final Map<String, Object[]> invocations = new HashMap<>();

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            invocations.put(method.getName(), args == null ? new Object[0] : args.clone());
            return null;
        }

        private Object argument(String methodName, int index) {
            return invocations.get(methodName)[index];
        }
    }
}
