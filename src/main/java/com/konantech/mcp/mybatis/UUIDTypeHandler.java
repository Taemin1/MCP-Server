  package com.konantech.mcp.mybatis;

  import org.apache.ibatis.type.BaseTypeHandler;
  import org.apache.ibatis.type.JdbcType;
  import org.apache.ibatis.type.MappedJdbcTypes;
  import org.apache.ibatis.type.MappedTypes;

  import java.sql.*;
  import java.util.UUID;

  @MappedTypes(UUID.class)
  @MappedJdbcTypes(value = JdbcType.OTHER, includeNullJdbcType = true)
  public class UUIDTypeHandler extends BaseTypeHandler<UUID> {

      @Override
      public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType)
              throws SQLException {
          // PostgreSQL uuid 컬럼에 맞게 UUID를 그대로 넘기거나 문자열로 넘겨도 됩니다
          ps.setObject(i, parameter, Types.OTHER);
      }

      @Override
      public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
          Object obj = rs.getObject(columnName);
          return obj == null ? null : UUID.fromString(obj.toString());
      }

      @Override
      public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
          Object obj = rs.getObject(columnIndex);
          return obj == null ? null : UUID.fromString(obj.toString());
      }

      @Override
      public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
          Object obj = cs.getObject(columnIndex);
          return obj == null ? null : UUID.fromString(obj.toString());
      }
  }