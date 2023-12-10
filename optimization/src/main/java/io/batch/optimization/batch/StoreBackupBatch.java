package io.batch.optimization.batch;


import io.batch.optimization.domain.Store;
import io.batch.optimization.domain.StoreHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 조건에 맞는 Store 조회해서 StoreHistory로 백업하는 단순한 배치
 * JPA N+1 문제 발생 -> FETCH JOIN 두번으로 해결하려고 함 -> MultipleBagFetchException 발생
 * 한번에 2개 이상의 자식 엔티티에는 join fetch을 사용할 수 없어 발생하는 문제
 * -> default_batch_fetch_size 설정으로 해결
 */
@RequiredArgsConstructor
@Configuration
public class StoreBackupBatch {

    private static final String JOB_NAME = "storeBackupJob";
    private static final String STEP_NAME = JOB_NAME + "Step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${chunkSize:1000}") // 없으면 기본값 1000, 있으면 설정한 값
    private int chunkSize;

    private static String ADDRESS_PARAM = null;

    @Bean
    public Job storeBackupJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(storeBackupStep())
                .build();
    }

    @Bean
    public Step storeBackupStep() {
        return stepBuilderFactory.get(STEP_NAME)
                .<Store, StoreHistory>chunk(chunkSize)
                .reader(storeBackupReader(ADDRESS_PARAM))
                .processor(storeBackupProcessor())
                .writer(storeBackupWriter())
                .build();
    }

    @JobScope
    @Bean
    public JpaPagingItemReader<Store> storeBackupReader(@Value("#{jobParameters[address]}") String address) {
        /**
         * LinkedHashMap: HashMap의 모든 기능을 포함하며 추가로 이중 연결 리스트를 사용하여 요소의 삽입 순서 또는 액세스 순서를 유지합니다.
         * 사용 이유 추측 : 파라미터가 더 추가 되었을 때 순서 유지를 위해 사용한 것 같음.
         * Hashmap이 LinkedHashMap과 비교했을 때 장점 : 추가적인 연결 요소를 유지할 필요가 없어서 약간 더 빠른 접근 시간을 제공. (HashMap은 순서를 고려하지 않는 일반적인 맵 사용 사례에 적합)
         */
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("address", address+"%");

        JpaPagingItemReaderBuilder<Store> itemReaderBuilder = new JpaPagingItemReaderBuilder<>();
        return itemReaderBuilder.name("storeBackupReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT s FROM Store s JOIN FETCH s.products p JOIN FETCH s.employees e WHERE s.address like :address")
                .parameterValues(parameters)
                .build();
    }

    @Bean
    public ItemProcessor<Store, StoreHistory> storeBackupProcessor() {
        return store -> new StoreHistory(store, store.getProducts(), store.getEmployees());
    }

    @Bean
    public JpaItemWriter<StoreHistory> storeBackupWriter() {
        JpaItemWriter<StoreHistory> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}