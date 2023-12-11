package io.batch.n_1.batch.reader;

import io.batch.n_1.domain.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JpaPagingItemReader를 사용, 하위 엔티티 2개를 가져오는 과정에서 N+1 문제 발생
 * 1. FETCH JOIN 으로 문제를 해결하려고 함. -> MultipleBagFetchException 발생 (한번에 2개 이상의 자식 엔티티에는 join fetch을 사용할 수 없음)
 * 2. batch size로 문제를 해결하려고 함. -> JpaPagingItemReader는 in query가 작동되지 않고 N+1 문제가 지속적으로 발생함.
 * JpaPagingItemReader는 내부에서 각 페이지 로드 시마다 별도의 트랜잭션을 시작하고 종료함. 트랜잭션이 페이지마다 종료되고 batch-size 설정이 유지되지 않음
 */
@RequiredArgsConstructor
@Configuration
public class StoreJpaPagingReaderConfig {

    private final EntityManagerFactory entityManagerFactory;

    @StepScope
    @Bean(name = "storeJpaPagingReader")
    public JpaPagingItemReader<Store> reader(
            @Value("#{jobParameters[address]}") String address,
            @Value("${chunkSize:1000}") int chunkSize
    ) {
        Map<String, Object> parameters = new LinkedHashMap<>(); // 파라미터가 더 추가 되었을 때 순서 유지를 위해 사용한 것 같음.
        parameters.put("address", address+"%");

        JpaPagingItemReaderBuilder<Store> itemReaderBuilder = new JpaPagingItemReaderBuilder<>();
        return itemReaderBuilder.name("storeBackupReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT s FROM Store s WHERE s.address like :address")
                .parameterValues(parameters)
                .build();
    }
}
