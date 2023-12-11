package n_1.batch.reader;

import n_1.domain.Store;
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
 * JpaPagingItemReader 사용 - 내부 트랜잭션으로 인해 batch size가 적용되지 않음
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
