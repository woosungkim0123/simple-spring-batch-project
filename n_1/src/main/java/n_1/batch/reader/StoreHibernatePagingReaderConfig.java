package n_1.batch.reader;

import n_1.domain.Store;
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
