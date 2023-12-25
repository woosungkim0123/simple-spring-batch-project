package optimization.job;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import optimization.domain.Product;
import optimization.domain.ProductBackup;
import optimization.reader.QuerydslPagingItemReader;
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

import static optimization.domain.QProduct.product;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = QuerydslPagingItemReaderConfiguration.JOB_NAME)
@Configuration
public class QuerydslPagingItemReaderConfiguration {

    public static final String JOB_NAME = "querydslPagingItemReaderBatchJob";
    public static final String STEP_NAME = "querydslPagingItemReaderBatchStep";
    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final QuerydslPagingItemReaderJobParameter jobParameter;

    private int chunkSize;

    @Value("${chunkSize:1000}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean
    @JobScope
    public QuerydslPagingItemReaderJobParameter jobParameter() {
        return new QuerydslPagingItemReaderJobParameter();
    }

    @Bean
    public Job job() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step())
                .build();
    }

    @Bean
    public Step step() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<Product, ProductBackup>chunk(chunkSize, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public QuerydslPagingItemReader<Product> reader() {
        return new QuerydslPagingItemReader<>(entityManagerFactory, chunkSize, queryFactory -> queryFactory
                .selectFrom(product)
                .where(product.createDate.eq(jobParameter.getDate()))
        );
    }

    private ItemProcessor<Product, ProductBackup> processor() {
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
