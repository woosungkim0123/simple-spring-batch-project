package n_1.batch.job;


import lombok.RequiredArgsConstructor;

import n_1.batch.reader.StoreCustomJpaPagingReaderConfig;
import n_1.domain.Store;
import n_1.domain.StoreHistory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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


/**
 * CustomJpaPagingItemReader를 만들어서 내부의 트랜잭션을 제거하고 HibernatePagingItemReader처럼 chunk 단위로 처리하도록 변경
 * batch size가 적용되어 N+1 문제가 발생하지 않는 걸 보여주는 배치
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = CustomJpaPagingReaderBatch.JOB_NAME)
@Configuration
public class CustomJpaPagingReaderBatch {

    public static final String JOB_NAME = "customJpaPagingReaderBatchJob";
    private static final String STEP_NAME = JOB_NAME + "Step";
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final StoreCustomJpaPagingReaderConfig reader;
    private final ItemProcessor<Store, StoreHistory> processor;
    private final JpaItemWriter<StoreHistory> writer;

    @Value("${chunkSize:1000}") // 없으면 기본값 1000, 있으면 설정한 값
    private int chunkSize;
    private static String ADDRESS_PARAM = null;

    @Bean(name = JOB_NAME)
    public Job job() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step step() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<Store, StoreHistory>chunk(chunkSize, transactionManager)
                .reader(reader.reader(ADDRESS_PARAM, chunkSize))
                .processor(processor)
                .writer(writer)
                .build();
    }
}