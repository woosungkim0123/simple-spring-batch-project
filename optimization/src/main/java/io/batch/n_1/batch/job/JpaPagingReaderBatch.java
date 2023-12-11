package io.batch.n_1.batch.job;

import io.batch.n_1.batch.reader.StoreJpaPagingReaderConfig;
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


import static io.batch.n_1.batch.job.JpaPagingReaderBatch.JOB_NAME;

/**
 * batch size가 적용되지 않아 N+1 문제가 발생하는 것을 확인하기 위한 예시
 * test/.../job/JpaPagingReaderBatchTest에서 테스트 가능
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = JOB_NAME)
@Configuration
public class JpaPagingReaderBatch {

    public static final String JOB_NAME = "jpaPagingReaderBatch";
    private static final String STEP_NAME = JOB_NAME + "Step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final StoreJpaPagingReaderConfig storeJpaPagingReaderConfig;
    private final ItemProcessor<Store, StoreHistory> processor;
    private final JpaItemWriter<StoreHistory> writer;

    @Value("${chunkSize:1000}") // 없으면 기본값 1000, 있으면 설정한 값
    private int chunkSize;
    private static String ADDRESS_PARAM = null;

    @Bean
    public Job jpaPagingReaderJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(jpaPagingReaderStep())
                .build();
    }

    @Bean
    public Step jpaPagingReaderStep() {
        return stepBuilderFactory.get(STEP_NAME)
                .<Store, StoreHistory>chunk(chunkSize)
                .reader(storeJpaPagingReaderConfig.reader(ADDRESS_PARAM, chunkSize))
                .processor(processor)
                .writer(writer)
                .build();
    }
}