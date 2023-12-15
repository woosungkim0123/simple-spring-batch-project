package n_1.batch.job;



import lombok.RequiredArgsConstructor;
import n_1.batch.reader.StoreJpaPagingReaderConfig;
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
 * N+1 문제 발생 예시를 보여주는 배치
 * 1. join fetch를 사용해 하위 엔티티 2개를 같이 조회 -> MultipleBagFetchException -> join fetch는 하나의 자식에게만 가능 <해결 x>
 * 2. batch size 적용 -> JpaPagingItemReader는 내부 트랜잭션으로 인해서 batch size가 적용되지 않음 <해결 x>
 * => JpaPagingItemReader는 내부에서 각 페이지 로드 시마다 별도의 트랜잭션을 시작하고 종료한다. 이 때문에 batch size 설정이 유지되지 않는다.
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = JpaPagingReaderBatch.JOB_NAME)
@Configuration
public class JpaPagingReaderBatch {

    public static final String JOB_NAME = "jpaPagingReaderBatchJob";
    private static final String STEP_NAME = JOB_NAME + "Step";
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StoreJpaPagingReaderConfig reader;
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