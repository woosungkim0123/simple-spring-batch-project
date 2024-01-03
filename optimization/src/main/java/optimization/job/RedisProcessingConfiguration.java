package optimization.job;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import optimization.domain.Product;
import optimization.domain.ProductBackup;
import optimization.job.parameter.JdbcCursorItemReaderJobParameter;
import optimization.mapper.ProductRowMapper;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Set;

/**
 * Redis를 이용해서 날짜별로 데이터를 합산하는 배치
 * 100만개 간단한 sum 요청: 11m11s528ms
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = RedisProcessingConfiguration.JOB_NAME)
@Configuration
public class RedisProcessingConfiguration {

    public static final String JOB_NAME = "redisProcessingBatchJob";
    public static final String STEP_NAME = "redisProcessingBatchStep";
    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final JdbcCursorItemReaderJobParameter jobParameter;
    private final DataSource dataSource;
    private final RedisTemplate<String, Long> redisTemplate;
    private static final String redisKeyPrefix = "normal_product_amount_sum:";

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
                .<Product, Product>chunk(chunkSize, transactionManager)
                .reader(reader())
                .writer(redisItemWriter())
                .listener(redisToDatabaseSaver())
                .build();
    }

    /**
     * 입력된 날짜보다 크거나 같은 데이터를 조회한다.
     */
    @JobScope
    @Bean
    public JdbcCursorItemReader<Product> reader() {
        return new JdbcCursorItemReaderBuilder<Product>()
                .name("jdbcCursorItemReader")
                .fetchSize(chunkSize)
                .sql("SELECT * FROM product WHERE create_date >= ? ORDER BY id ASC")
                .rowMapper(new ProductRowMapper())
                .dataSource(dataSource)
                .preparedStatementSetter(ps -> ps.setDate(1, Date.valueOf(jobParameter.getDate())))
                .build();
    }

    @Bean
    public ItemWriter<Product> redisItemWriter() {
        return products -> {
            ValueOperations<String, Long> ops = redisTemplate.opsForValue();
            for (Product product : products) {
                String key = redisKeyPrefix + product.getCreateDate();
                ops.increment(key, product.getAmount());
            }
        };
    }
    
    @Bean
    public StepExecutionListener redisToDatabaseSaver() {
        return new StepExecutionListener() {
            @Override
            public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();

                try {
                    Set<String> keys = redisTemplate.keys(redisKeyPrefix + "*");
                    ValueOperations<String, Long> valueOps = redisTemplate.opsForValue();

                    for (String key : keys) {
                        Long totalAmount = valueOps.get(key);
                        LocalDate createDate = LocalDate.parse(key.split(":")[1]);

                        ProductBackup backup = ProductBackup.builder()
                                .amount(totalAmount)
                                .createDate(createDate)
                                .build();
                        entityManager.merge(backup);
                    }

                    transaction.commit();
                } catch (Exception e) {
                    transaction.rollback();
                    throw e;
                } finally {
                    entityManager.close();
                }
                return ExitStatus.COMPLETED;
            }
        };
    }
}
