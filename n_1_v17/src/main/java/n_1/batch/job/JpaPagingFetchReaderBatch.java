package n_1.batch.job;


import lombok.RequiredArgsConstructor;
import n_1.batch.reader.StoreJpaPagingFetchReaderConfig;
import n_1.domain.StoreHistory;
import n_1.domain.list_change_set_domain.StoreSet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
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
 * List 대신 Set 사용하는 예시
 * 페이징(firstResult와 maxResults)과 fetch join을 같이 사용하면 페이징 쿼리가 나가지 않고
 * 모든 데이터를 메모리 상에 다 불러와서 페이징 처리를 하게 된다.
 * 많은 양의 데이터에 대해 매우 비효율적이며 성능 문제를 일으킬 수 있다.
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = JpaPagingFetchReaderBatch.JOB_NAME)
@Configuration
public class JpaPagingFetchReaderBatch {

    public static final String JOB_NAME = "jpaPagingFetchReaderBatchJob";
    private static final String STEP_NAME = JOB_NAME + "Step";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final StoreJpaPagingFetchReaderConfig reader;
    private final ItemProcessor<StoreSet, StoreHistory> processor;
    private final JpaItemWriter<StoreHistory> writer;

    @Value("${chunkSize:1000}")
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
                .<StoreSet, StoreHistory>chunk(chunkSize, transactionManager)
                .reader(reader.reader(ADDRESS_PARAM, chunkSize))
                .processor(processor)
                .writer(writer)
                .build();
    }
}