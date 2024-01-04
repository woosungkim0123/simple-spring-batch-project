package optimization.job;

import lombok.RequiredArgsConstructor;
import optimization.domain.Product;
import optimization.domain.ProductBackup;
import optimization.job.parameter.JdbcCursorItemReaderJobParameter;
import optimization.mapper.ProductRowMapper;
import optimization.writer.CustomJdbcBatchItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * JDBC ExecuteBatch를 이용해서 Writer를 개선한 배치
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = JdbcExecuteBatchWriterConfiguration.JOB_NAME)
@Configuration
public class JdbcExecuteBatchWriterConfiguration {

    public static final String JOB_NAME = "jdbcExecuteBatchWriterBatchJob";
    public static final String STEP_NAME = "jdbcExecuteBatchWriterBatchStep";
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JdbcCursorItemReaderJobParameter jobParameter;
    private final DataSource dataSource;

    private int chunkSize;

    @Value("${chunkSize:1000}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean(name = "JdbcCursorItemReaderJobParameter")
    @JobScope
    public JdbcCursorItemReaderJobParameter jobParameter() {
        return new JdbcCursorItemReaderJobParameter();
    }

    @Bean(name = JOB_NAME)
    public Job job() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step step() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<Product, ProductBackup>chunk(chunkSize, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @JobScope
    @Bean
    public JdbcCursorItemReader<Product> reader() {
        return new JdbcCursorItemReaderBuilder<Product>()
                .name("jdbcCursorItemReader")
                .fetchSize(chunkSize)
                .sql("SELECT * FROM product WHERE create_date = ? ORDER BY id ASC")
                .rowMapper(new ProductRowMapper())
                .dataSource(dataSource)
                .preparedStatementSetter(ps -> ps.setDate(1, Date.valueOf(jobParameter.getDate())))
                .build();
    }

    private ItemProcessor<Product, ProductBackup> processor() {
        return product -> ProductBackup.builder()
                .name(product.getName() + "_backup")
                .amount(product.getAmount())
                .createDate(product.getCreateDate())
                .build();
    }

    @Bean
    public CustomJdbcBatchItemWriter<ProductBackup> writer() {
        String sql = "INSERT INTO product_backup (name, amount, create_date) VALUES (?, ?, ?)";

        return new CustomJdbcBatchItemWriter<>(dataSource, sql, (PreparedStatement ps, ProductBackup item) -> {
            try {
                ps.setString(1, item.getName());
                ps.setLong(2, item.getAmount());
                ps.setDate(3, Date.valueOf(item.getCreateDate()));
            } catch (SQLException e) {
                throw new RuntimeException("Error setting PreparedStatement values", e);
            }
        });
    }
}
