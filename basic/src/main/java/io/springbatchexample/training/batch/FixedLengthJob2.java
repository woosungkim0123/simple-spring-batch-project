package io.springbatchexample.training.batch;

import io.springbatchexample.training.dto.TwoLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

/**
 * 고정된 길이를 특정 구간 만큼 잘라서 변환 후 새로운 파일로 내보내는 배치
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class FixedLengthJob2 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private static final int chunkSize = 5;

    @Bean
    public Job fixedLengthJob2_batchBuild() {
        return jobBuilderFactory.get("fixedLengthJob2")
                .start(fixedLengthJob2_batchStep1())
                .build();
    }

    @Bean
    public Step fixedLengthJob2_batchStep1() {
        return stepBuilderFactory.get("fixedLengthJob2_batchStep1")
                .<TwoLineDto, String>chunk(chunkSize)
                .reader(fixedLengthJob2_FileReader())
                .processor(fixedLengthJob2_Processor())
                .writer(fixedLengthJob2_FileWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<TwoLineDto> fixedLengthJob2_FileReader() {
        FlatFileItemReader<TwoLineDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/fixedLengthJob2_input.txt"));
        flatFileItemReader.setLinesToSkip(1);

        DefaultLineMapper<TwoLineDto> lineMapper = new DefaultLineMapper<>();

        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setNames("one", "two");
        tokenizer.setColumns(new Range(1, 5), new Range(6, 10));

        BeanWrapperFieldSetMapper<TwoLineDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(TwoLineDto.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);

        flatFileItemReader.setLineMapper(lineMapper);

        return flatFileItemReader;
    }

    @Bean
    public ItemProcessor<TwoLineDto, String> fixedLengthJob2_Processor() {
        return item -> {
            String formattedOne = String.format("%-5s", item.getOne());
            String formattedTwo = String.format("%5s", item.getTwo());
            return formattedOne + "###" + formattedTwo;
        };
    }

    @Bean
    public FlatFileItemWriter<String> fixedLengthJob2_FileWriter() {
        LineAggregator<String> lineAggregator = new PassThroughLineAggregator<>();

        return new FlatFileItemWriterBuilder<String>()
                .name("fixedLengthJob2_FileWriter")
                .resource(new FileSystemResource("output/fixedLengthJob2_output.txt"))
                .lineAggregator(lineAggregator)
                .build();
    }
}
