package optimization.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * jdbcTemplate을 이용하여 페이징 처리를 하는 Reader
 */
public class CustomJdbcPagingItemReader<T> implements ItemReader<T> {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final String query;
    private final RowMapper<T> rowMapper;
    private int page;
    private final int pageSize;
    private int currentItemIndex;
    private List<T> items;
    private LocalDate date;

    public CustomJdbcPagingItemReader(DataSource dataSource, String query, RowMapper<T> rowMapper, int pageSize) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.query = query;
        this.rowMapper = rowMapper;
        this.page = 0;
        this.pageSize = pageSize;
        this.currentItemIndex = 0;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public T read() {
        if (items == null || currentItemIndex >= items.size()) {
            fetchNextPage();
        }

        if (items == null || items.isEmpty()) {
            return null; // 더 이상 읽을 데이터가 없음
        }

        return items.get(currentItemIndex++);
    }

    private void fetchNextPage() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", page * pageSize);
        parameters.put("pageSize", pageSize);
        parameters.put("date", date);
        items = jdbcTemplate.query(query, parameters, rowMapper);
        currentItemIndex = 0;
        page++;
    }
}