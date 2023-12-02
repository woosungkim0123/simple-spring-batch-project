package io.springbatchexample.training.batch;

import io.springbatchexample.training.dto.CoinMarket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JsonJob1 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private static final int chunkSize = 5;

    @Bean
    public Job jsonJob1_batchBuild() {
        return jobBuilderFactory.get("jsonJob1")
                .start(jsonJob1_batchStep1())
                .build();
    }

    @Bean
    public Step jsonJob1_batchStep1() {
        return stepBuilderFactory.get("jsonJob1_batchStep1")
                .<CoinMarket, CoinMarket>chunk(chunkSize)
                .reader(jsonJob1_jsonReader())
                .writer(coinMarkets -> coinMarkets.stream().forEach(i -> {
                    log.info("-> {}", i.toString());
                }))
                .build();
    }

    @Bean
    public JsonItemReader<CoinMarket> jsonJob1_jsonReader() {
        return new JsonItemReaderBuilder<CoinMarket>()
                .name("jsonJob1_jsonReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(CoinMarket.class))
                .resource(new ClassPathResource("sample/jsonJob1_input.json"))
                .build();
    }
}
