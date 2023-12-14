package n_1.batch.job;

import lombok.RequiredArgsConstructor;
import n_1.batch.reader.StoreCustomJpaPagingReaderConfig;
import n_1.domain.Store;
import n_1.domain.StoreHistory;
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

import static n_1.batch.job.CustomJpaPagingReaderBatch.JOB_NAME;


/**
 * CustomJpaPagingItemReader를 만들어서 내부의 트랜잭션을 제거하고 HibernatePagingItemReader처럼 chunk 단위로 처리하도록 변경
 * batch size가 적용되어 N+1 문제가 발생하지 않는 걸 보여주는 배치
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = JOB_NAME)
@Configuration
public class CustomJpaPagingReaderBatch {

    public static final String JOB_NAME = "customJpaPagingReaderBatchJob";
    private static final String STEP_NAME = JOB_NAME + "Step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final StoreCustomJpaPagingReaderConfig reader;
    private final ItemProcessor<Store, StoreHistory> processor;
    private final JpaItemWriter<StoreHistory> writer;

    @Value("${chunkSize:1000}") // 없으면 기본값 1000, 있으면 설정한 값
    private int chunkSize;
    private static String ADDRESS_PARAM = null;

    @Bean(name = JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(step())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step step() {
        return stepBuilderFactory.get(STEP_NAME)
                .<Store, StoreHistory>chunk(chunkSize)
                .reader(reader.reader(ADDRESS_PARAM, chunkSize))
                .processor(processor)
                .writer(writer)
                .build();
    }
}