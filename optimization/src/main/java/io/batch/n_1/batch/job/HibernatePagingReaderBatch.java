package io.batch.n_1.batch.job;

import io.batch.n_1.batch.reader.StoreHibernatePagingReaderConfig;

import io.batch.n_1.domain.Store;
import io.batch.n_1.domain.StoreHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.batch.n_1.batch.job.HibernatePagingReaderBatch.JOB_NAME;


/**
 * HibernatePagingItemReader는 batch size가 적용되어 N+1 문제가 발생하지 않는 걸 보여주는 예시
 * test/.../job/HibernatePagingReaderBatchTest에서 테스트 가능
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = JOB_NAME)
@Configuration
public class HibernatePagingReaderBatch {

    public static final String JOB_NAME = "hibernatePagingReaderBatch";
    private static final String STEP_NAME = JOB_NAME + "Step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final StoreHibernatePagingReaderConfig storeHibernatePagingReaderConfig;
    private final ItemProcessor<Store, StoreHistory> processor;
    private final JpaItemWriter<StoreHistory> writer;

    @Value("${chunkSize:1000}")
    private int chunkSize;
    private static String ADDRESS_PARAM = null;

    @Bean
    public Job hibernatePagingReaderBatchJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(hibernatePagingReaderBatchStep())
                .build();
    }

    @Bean
    public Step hibernatePagingReaderBatchStep() {
        return stepBuilderFactory.get(STEP_NAME)
                .<Store, StoreHistory>chunk(chunkSize)
                .reader(storeHibernatePagingReaderConfig.reader(ADDRESS_PARAM, chunkSize))
                .processor(processor)
                .writer(writer)
                .build();
    }
}