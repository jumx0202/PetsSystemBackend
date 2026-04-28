package ynu.pet.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import ynu.pet.entity.LostPost;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LostPostStatusTypeHandler extends BaseTypeHandler<LostPost.LostStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LostPost.LostStatus parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public LostPost.LostStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return fromValue(rs.getInt(columnName), rs.wasNull());
    }

    @Override
    public LostPost.LostStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return fromValue(rs.getInt(columnIndex), rs.wasNull());
    }

    @Override
    public LostPost.LostStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return fromValue(cs.getInt(columnIndex), cs.wasNull());
    }

    private LostPost.LostStatus fromValue(int value, boolean wasNull) {
        if (wasNull) {
            return null;
        }
        for (LostPost.LostStatus status : LostPost.LostStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }
}
