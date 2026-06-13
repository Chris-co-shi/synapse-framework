package com.indigo.synapse.data.json;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@MappedTypes(JsonValue.class)
@MappedJdbcTypes(JdbcType.OTHER)
public final class JsonValueTypeHandler extends BaseTypeHandler<JsonValue> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JsonValue parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.value(), Types.OTHER);
    }

    @Override
    public JsonValue getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return JsonValue.ofNullable(rs.getString(columnName));
    }

    @Override
    public JsonValue getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return JsonValue.ofNullable(rs.getString(columnIndex));
    }

    @Override
    public JsonValue getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return JsonValue.ofNullable(cs.getString(columnIndex));
    }
}
