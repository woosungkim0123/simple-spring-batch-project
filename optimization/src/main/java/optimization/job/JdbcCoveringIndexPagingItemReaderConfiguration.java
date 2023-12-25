package optimization.job;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import optimization.domain.Product;
import optimization.domain.ProductBackup;
import optimization.job.parameter.JdbcPagingItemReaderJobParameter;
import optimization.mapper.ProductRowMapper;
import optimization.reader.CustomJdbcPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * JDBC를 이용하여 페이징 처리를 하는 배치
 * 커버링 인덱스 쿼리를 통한 성능 개선 적용
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = JdbcCoveringIndexPagingItemReaderConfiguration.JOB_NAME)
@Configuration
public class JdbcCoveringIndexPagingItemReaderConfiguration {

    public static final String JOB_NAME = "jdbcCoveringIndexPagingItemReaderBatchJob";
    public static final String STEP_NAME = "jdbcCoveringIndexPagingItemReaderBatchStep";
    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final JdbcPagingItemReaderJobParameter jobParameter;
    private final DataSource dataSource;

    private int chunkSize;

    @Value("${chunkSize:2}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean(name = "jdbcPagingItemReaderJobParameter")
    @JobScope
    public JdbcPagingItemReaderJobParameter jobParameter() {
        return new JdbcPagingItemReaderJobParameter();
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
    public CustomJdbcPagingItemReader<Product> reader() {
        String query = "SELECT p.id, p.name, p.amount, p.create_date FROM product p JOIN (SELECT id FROM product WHERE create_date = :date ORDER BY id ASC LIMIT :page, :pageSize) as temp on temp.id = p.id";
        CustomJdbcPagingItemReader<Product> reader = new CustomJdbcPagingItemReader<>(dataSource, query, new ProductRowMapper(), chunkSize);
        reader.setDate(jobParameter.getDate());

        return reader;
    }

    private ItemProcessor<Product, ProductBackup> processor() {
        System.out.println("processor");
        return product -> ProductBackup.builder()
                .name(product.getName() + "_backup")
                .amount(product.getAmount())
                .createDate(product.getCreateDate())
                .build();
    }

    @Bean
    public JpaItemWriter<ProductBackup> writer() {
        JpaItemWriter<ProductBackup> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
