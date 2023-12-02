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
 * csv를 특수문자를 구분으로 읽고, 특정한 형태로 변형 후 파일로 내보내는 배치
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvJob2 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private static final int chunkSize = 5;

    @Bean
    public Job csvJob2_batchBuild() {
        return jobBuilderFactory.get("csvJob2")
                .start(csvJob2_batchStep1())
                .build();
    }

    @Bean
    public Step csvJob2_batchStep1() {
        return stepBuilderFactory.get("csvJob2_batchStep1")
                .<TwoLineDto, String>chunk(chunkSize)
                .reader(csvJob2_FileReader())
                .processor(csvJob2_Processor())
                .writer(csvJob2_FileWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<TwoLineDto> csvJob2_FileReader() {
        FlatFileItemReader<TwoLineDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/csvJob2_input.csv"));
        flatFileItemReader.setLinesToSkip(1);

        DefaultLineMapper<TwoLineDto> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("one", "two");
        tokenizer.setDelimiter(":");

        BeanWrapperFieldSetMapper<TwoLineDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(TwoLineDto.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);

        flatFileItemReader.setLineMapper(lineMapper);

        return flatFileItemReader;
    }

    @Bean
    public ItemProcessor<TwoLineDto, String> csvJob2_Processor() {
        return item -> item.getOne() + "@" + item.getTwo();
    }

    @Bean
    public FlatFileItemWriter<String> csvJob2_FileWriter() {
        LineAggregator<String> lineAggregator = new PassThroughLineAggregator<>();

        return new FlatFileItemWriterBuilder<String>()
                .name("csvJob2_FileWriter")
                .resource(new FileSystemResource("output/csvJob2_output.csv"))
                .lineAggregator(lineAggregator)
                .build();
    }
}
