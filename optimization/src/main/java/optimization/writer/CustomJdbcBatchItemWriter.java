package optimization.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;

public class CustomJdbcBatchItemWriter<T> implements ItemWriter<T> {
    private DataSource dataSource;
    private String sql;
    private BiConsumer<PreparedStatement, T> preparedStatementSetter;

    public CustomJdbcBatchItemWriter(DataSource dataSource, String sql,
                                     BiConsumer<PreparedStatement, T> preparedStatementSetter) {
        this.dataSource = dataSource;
        this.sql = sql;
        this.preparedStatementSetter = preparedStatementSetter;
    }

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        List<? extends T> items = chunk.getItems();
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dataSource.getConnection(); // 커넥션 획득
            connection.setAutoCommit(false); // 자동 커밋 비활성화
            preparedStatement = connection.prepareStatement(this.sql); //sql 로 prepared statement 생성

            for (T item : items) {
                preparedStatementSetter.accept(preparedStatement, item); // prepared statement에 파라미터 바인딩
                preparedStatement.addBatch(); // batch에 prepared statement 추가
            }

            preparedStatement.executeBatch(); // 배치에 추가된 모든 SQL 문을 데이터베이스에 한 번에 전송
            connection.commit(); // 트랜잭션을 커밋
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw new Exception("Error executing batch write", e);
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
