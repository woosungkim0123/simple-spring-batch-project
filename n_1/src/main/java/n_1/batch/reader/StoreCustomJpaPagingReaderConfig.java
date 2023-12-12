package n_1.batch.reader;

import lombok.RequiredArgsConstructor;
import n_1.batch.custom.CustomJpaPagingItemReader;
import n_1.batch.custom.CustomJpaPagingItemReaderBuilder;
import n_1.domain.Store;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CustomJpaPagingItemReaderBuilder 사용 - 내부 트랜잭션 제거 batch size 적용됨
 */
@RequiredArgsConstructor
@Configuration
public class StoreCustomJpaPagingReaderConfig {

    private final EntityManagerFactory entityManagerFactory;

    @StepScope
    @Bean(name = "storeCustomJpaPagingReader")
    public CustomJpaPagingItemReader<Store> reader(
            @Value("#{jobParameters[address]}") String address,
            @Value("${chunkSize:1000}") int chunkSize
    ) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("address", address+"%");

        CustomJpaPagingItemReaderBuilder<Store> itemReaderBuilder = new CustomJpaPagingItemReaderBuilder<>();
        return itemReaderBuilder.name("storeBackupReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT s FROM Store s WHERE s.address like :address")
                .parameterValues(parameters)
                .build();
    }
}
