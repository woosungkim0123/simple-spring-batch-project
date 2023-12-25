package io.springbatchexample.training.batch;

import io.springbatchexample.training.domain.Dept1;
import io.springbatchexample.training.dto.TwoLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;


/**
 * 단일 csv 파일 내용을 db로 옮기는 배치
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvToJpaJob1 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private static final int chunkSize = 5;

    @Bean
    public Job csvToJpaJob1_batchBuild() {
        return jobBuilderFactory.get("csvToJpaJob1")
                .start(csvToJpaJob1_batchStep1())
                .build();
    }

    @Bean
    public Step csvToJpaJob1_batchStep1() {
        return stepBuilderFactory.get("csvToJpaJob1_batchStep1")
                .<TwoLineDto, Dept1>chunk(chunkSize)
                .reader(csvToJpaJob1_FileReader())
                .processor(csvToJpaJob1_Processor())
                .writer(csvToJpaJob1_FileWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<TwoLineDto> csvToJpaJob1_FileReader() {
        FlatFileItemReader<TwoLineDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/csvToJpaJob1_input.csv"));
        // flatFileItemReader.setLinesToSkip(1);

        DefaultLineMapper<TwoLineDto> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("one", "two");
        tokenizer.setDelimiter(":");
        lineMapper.setLineTokenizer(tokenizer);

        BeanWrapperFieldSetMapper<TwoLineDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(TwoLineDto.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        flatFileItemReader.setLineMapper(lineMapper);

        return flatFileItemReader;
    }

    @Bean
    public ItemProcessor<TwoLineDto, Dept1> csvToJpaJob1_Processor() {
        return twoLineDto -> new Dept1(Long.parseLong(twoLineDto.getOne()), twoLineDto.getTwo(), "test");
    }

    @Bean
    public JpaItemWriter<Dept1> csvToJpaJob1_FileWriter() {
        JpaItemWriter<Dept1> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
