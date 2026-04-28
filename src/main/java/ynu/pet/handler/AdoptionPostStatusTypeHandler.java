package ynu.pet.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import ynu.pet.entity.AdoptionPost;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdoptionPostStatusTypeHandler extends BaseTypeHandler<AdoptionPost.PostStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AdoptionPost.PostStatus parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public AdoptionPost.PostStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return fromValue(rs.getInt(columnName), rs.wasNull());
    }

    @Override
    public AdoptionPost.PostStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return fromValue(rs.getInt(columnIndex), rs.wasNull());
    }

    @Override
    public AdoptionPost.PostStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return fromValue(cs.getInt(columnIndex), cs.wasNull());
    }

    private AdoptionPost.PostStatus fromValue(int value, boolean wasNull) {
        if (wasNull) {
            return null;
        }
        for (AdoptionPost.PostStatus status : AdoptionPost.PostStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }
}
