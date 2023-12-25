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
import org.springframework.batch.item.file.transform.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * 고정된 길이를 특정 구간 만큼 잘라서 출력하는 배치
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class FixedLengthJob1 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private static final int chunkSize = 5;

    @Bean
    public Job fixedLengthJob1_batchBuild() {
        return jobBuilderFactory.get("fixedLengthJob1")
                .start(fixedLengthJob1_batchStep1())
                .build();
    }

    @Bean
    public Step fixedLengthJob1_batchStep1() {
        return stepBuilderFactory.get("fixedLengthJob1_batchStep1")
                .<TwoLineDto, TwoLineDto>chunk(chunkSize)
                .reader(fixedLengthJob1_FileReader())
                .writer(twoDto -> twoDto.stream().forEach(i -> {
                    log.info("-> {}", i.toString());
                }))
                .build();
    }

    @Bean
    public FlatFileItemReader<TwoLineDto> fixedLengthJob1_FileReader() {
        FlatFileItemReader<TwoLineDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/fixedLengthJob1_input.txt"));
        flatFileItemReader.setLinesToSkip(1);

        DefaultLineMapper<TwoLineDto> lineMapper = new DefaultLineMapper<>();

        FixedLengthTokenizer fixedLengthTokenizer = new FixedLengthTokenizer();
        fixedLengthTokenizer.setNames("one", "two");
        fixedLengthTokenizer.setColumns(new Range(1, 5), new Range(6, 10));

        BeanWrapperFieldSetMapper<TwoLineDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(TwoLineDto.class);

        lineMapper.setLineTokenizer(fixedLengthTokenizer);
        lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);

        flatFileItemReader.setLineMapper(lineMapper);

        return flatFileItemReader;
    }
}
