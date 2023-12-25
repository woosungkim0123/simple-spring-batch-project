package optimization.mapper;

import optimization.domain.Product;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductRowMapper implements RowMapper<Product> {
    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Product.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .amount(rs.getLong("amount"))
                .createDate(rs.getDate("create_date").toLocalDate())
                .build();
    }
}
