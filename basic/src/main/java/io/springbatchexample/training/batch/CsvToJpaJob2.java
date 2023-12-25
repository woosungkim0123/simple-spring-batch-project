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
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;


/**
 * 멀티 csv 파일 내용을 db로 옮기는 배치
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvToJpaJob2 {

    private final ResourceLoader resourceLoader;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private static final int chunkSize = 5;

    @Bean
    public Job csvToJpaJob2_batchBuild() {
        return jobBuilderFactory.get("csvToJpaJob2")
                .start(csvToJpaJob2_batchStep1())
                .build();
    }

    @Bean
    public Step csvToJpaJob2_batchStep1() {
        return stepBuilderFactory.get("csvToJpaJob2_batchStep1")
                .<TwoLineDto, Dept1>chunk(chunkSize)
                .reader(csvToJpaJob2_FileReader())
                .processor(csvToJpaJob2_Processor())
                .writer(csvToJpaJob2_FileWriter())
                // 에러가 난 것도 무시하고 넘어가고 싶을 때
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(2)
                .build();
    }

    @Bean
    public MultiResourceItemReader<TwoLineDto> csvToJpaJob2_FileReader() {
        MultiResourceItemReader<TwoLineDto> multiResourceItemReader = new MultiResourceItemReader<>();

        try {
            multiResourceItemReader.setResources(ResourcePatternUtils.getResourcePatternResolver(
                    resourceLoader).getResources("classpath*:sample/csvToJpaJob2_input*.csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        multiResourceItemReader.setDelegate(multiFileItemReader());

        return multiResourceItemReader;
    }

    private FlatFileItemReader<TwoLineDto> multiFileItemReader() {
        FlatFileItemReader<TwoLineDto> flatFileItemReader = new FlatFileItemReader<>();

        flatFileItemReader.setLineMapper((line, lineNumber) -> {
            String[] lines = line.split(":");
            return new TwoLineDto(lines[0], lines[1]);
        });

        return flatFileItemReader;
    }

    @Bean
    public ItemProcessor<TwoLineDto, Dept1> csvToJpaJob2_Processor() {
        return twoLineDto -> new Dept1(Long.parseLong(twoLineDto.getOne()), twoLineDto.getTwo(), "test");
    }

    @Bean
    public JpaItemWriter<Dept1> csvToJpaJob2_FileWriter() {
        JpaItemWriter<Dept1> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
