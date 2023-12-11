package n_1.batch.processor;

import n_1.domain.Store;
import n_1.domain.StoreHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 공통적으로 사용되는 Processor
 */
@RequiredArgsConstructor
@Configuration
public class StoreHistoryProcessorConfig {

    @Bean
    public ItemProcessor<Store, StoreHistory> storeToStoreHistoryProcessor() {
        return store -> new StoreHistory(store, store.getProducts(), store.getEmployees());
    }
}
