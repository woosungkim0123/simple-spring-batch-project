package n_1.batch.reader;

import lombok.RequiredArgsConstructor;
import n_1.batch.custom.CustomJpaPagingItemReader;
import n_1.batch.custom.CustomJpaPagingItemReaderBuilder;
import n_1.domain.list_change_set_domain.StoreSet;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * List 대신 Set 사용 -> MultipleBagFetchException 발생 x
 */
@RequiredArgsConstructor
@Configuration
public class StoreCustomJpaPagingFetchReaderConfig {

    private final EntityManagerFactory entityManagerFactory;

    @StepScope
    @Bean(name = "storeCustomJpaPagingFetchReader")
    public CustomJpaPagingItemReader<StoreSet> reader(
            @Value("#{jobParameters[address]}") String address,
            @Value("${chunkSize:1000}") int chunkSize
    ) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("address", address+"%");

        CustomJpaPagingItemReaderBuilder<StoreSet> itemReaderBuilder = new CustomJpaPagingItemReaderBuilder<>();
        return itemReaderBuilder.name("storeCustomJpaPagingFetchReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT s FROM StoreSet s JOIN FETCH s.products p JOIN FETCH s.employees e WHERE s.address like :address")
                .parameterValues(parameters)
                .build();
    }
}
