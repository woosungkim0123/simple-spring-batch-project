package io.batch.optimization.batch.writer;

import io.batch.optimization.domain.StoreHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

/**
 * 해당 프로젝트에서 공통적으로 사용되는 Writer
 */
@RequiredArgsConstructor
@Configuration
public class StoreHistoryWriterConfig {

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public JpaItemWriter<StoreHistory> storeHistoryWriter() {
        JpaItemWriter<StoreHistory> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
