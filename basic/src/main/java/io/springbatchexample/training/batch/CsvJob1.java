package io.springbatchexample.training.batch;

import io.springbatchexample.training.dto.TwoLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * csv를 특수문자를 구분으로 읽고, 특정한 형식으로 변형 후 출력하는 배치
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvJob1 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private static final int chunkSize = 5;

    @Bean
    public Job csvJob1_batchBuild() {
        return jobBuilderFactory.get("csvJob1")
                .start(csvJob1_batchStep1())
                .build();
    }

    @Bean
    public Step csvJob1_batchStep1() {
        return stepBuilderFactory.get("csvJob1_batchStep1")
                .<TwoLineDto, TwoLineDto>chunk(chunkSize)
                .reader(csvJob1_FileReader())
                .writer(twoDto -> twoDto.stream().forEach(i -> {
                    log.info("-> {}", i.toString());
                }))
                .build();
    }

    @Bean
    public FlatFileItemReader<TwoLineDto> csvJob1_FileReader() {
        FlatFileItemReader<TwoLineDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/csvJob1_input.csv"));
        flatFileItemReader.setLinesToSkip(1); // 칼럼명을 제거하고 데이터만 가져오기

        DefaultLineMapper<TwoLineDto> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("one", "two"); // 컬럼명
        tokenizer.setDelimiter(":"); // 구분자

        BeanWrapperFieldSetMapper<TwoLineDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(TwoLineDto.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);

        flatFileItemReader.setLineMapper(lineMapper);

        return flatFileItemReader;
    }
}
