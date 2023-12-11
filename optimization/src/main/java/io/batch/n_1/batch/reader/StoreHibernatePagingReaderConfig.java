package io.batch.n_1.batch.reader;

import io.batch.n_1.domain.Store;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.HibernatePagingItemReader;
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
public class StoreHibernatePagingReaderConfig {

    private final EntityManagerFactory entityManagerFactory;

    @StepScope
    @Bean(name = "storeHibernatePagingReader")
    public HibernatePagingItemReader<Store> reader(
            @Value("#{jobParameters[address]}") String address,
            @Value("${chunkSize:1000}") int chunkSize
    ) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("address", address + "%");
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

        HibernatePagingItemReader<Store> reader = new HibernatePagingItemReader<>();
        reader.setQueryString("FROM Store s WHERE s.address LIKE :address");
        reader.setParameterValues(parameters);
        reader.setSessionFactory(sessionFactory);
        reader.setFetchSize(chunkSize);
        reader.setUseStatelessSession(false);

        return reader;
    }
}
