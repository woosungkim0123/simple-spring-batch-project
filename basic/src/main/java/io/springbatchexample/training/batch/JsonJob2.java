package io.springbatchexample.training.batch;

import io.springbatchexample.training.dto.CoinMarket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JsonJob2 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private static final int chunkSize = 5;

    @Bean
    public Job JsonJob2_batchBuild() {
        return jobBuilderFactory.get("JsonJob2")
                .start(JsonJob2_batchStep1())
                .build();
    }

    @Bean
    public Step JsonJob2_batchStep1() {
        return stepBuilderFactory.get("JsonJob2_batchStep1")
                .<CoinMarket, CoinMarket>chunk(chunkSize)
                .reader(JsonJob2_jsonReader())
                .processor(jsonJob2_processor())
                .writer(JsonJob2_jsonWriter())
                .build();
    }

    private ItemProcessor<CoinMarket, CoinMarket> jsonJob2_processor() {
        return coinMarket -> {
            // "KRW-"로 시작하지 않는 경우 null을 반환
            if (!coinMarket.getMarket().startsWith("KRW-")) {
                return null;
            }
            return coinMarket;
        };
    }

    @Bean
    public JsonItemReader<CoinMarket> JsonJob2_jsonReader() {
        return new JsonItemReaderBuilder<CoinMarket>()
                .name("JsonJob2_jsonReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(CoinMarket.class))
                .resource(new ClassPathResource("sample/JsonJob2_input.json"))
                .build();
    }

    @Bean
    public JsonFileItemWriter<CoinMarket> JsonJob2_jsonWriter() {
        return new JsonFileItemWriterBuilder<CoinMarket>()
                .name("JsonJob2_jsonWriter")
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(new FileSystemResource("output/jsonJob2_output.json"))
                .build();
    }
}
