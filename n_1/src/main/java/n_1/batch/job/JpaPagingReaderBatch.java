package n_1.batch.job;

import n_1.batch.reader.StoreJpaPagingReaderConfig;
import n_1.domain.Store;
import n_1.domain.StoreHistory;
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

import static n_1.batch.job.JpaPagingReaderBatch.JOB_NAME;


/**
 * N+1 문제 발생 예시를 보여주는 배치
 * 1. join fetch를 사용해 하위 엔티티 2개를 같이 조회 -> MultipleBagFetchException -> join fetch는 하나의 자식에게만 가능 <해결 x>
 * 2. batch size 적용 -> JpaPagingItemReader는 내부 트랜잭션으로 인해서 batch size가 적용되지 않음 <해결 x>
 * => JpaPagingItemReader는 내부에서 각 페이지 로드 시마다 별도의 트랜잭션을 시작하고 종료한다. 이 때문에 batch size 설정이 유지되지 않는다.
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.name", havingValue = JOB_NAME)
@Configuration
public class JpaPagingReaderBatch {

    public static final String JOB_NAME = "jpaPagingReaderBatchJob";
    private static final String STEP_NAME = JOB_NAME + "Step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final StoreJpaPagingReaderConfig reader;
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