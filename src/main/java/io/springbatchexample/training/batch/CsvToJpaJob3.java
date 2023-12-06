package io.springbatchexample.training.batch;

import io.springbatchexample.training.domain.Two;
import io.springbatchexample.training.dto.TwoLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;

/**
 * job.name=csvToJpaJob3 inFileName=INFILES/csvToJpaJob3.txt
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvToJpaJob3 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private static final int chunkSize = 5;

    @Bean
    public Job csvToJpaJob3_batchBuild() {
        return jobBuilderFactory.get("csvToJpaJob3")
                .start(csvToJpaJob3_batchStep1())
                .build();
    }

    @Bean
    public Step csvToJpaJob3_batchStep1() {
        return stepBuilderFactory.get("csvToJpaJob3_batchStep1")
                .<TwoLineDto, Two>chunk(chunkSize)
                .reader(csvToJpaJob3_reader(null))
                .processor(csvToJpaJob3_processor())
                .writer(csvToJpaJob3_dbItemWriter())
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<TwoLineDto> csvToJpaJob3_reader(@Value("#{jobParameters[inFileName]}") String inFileName) {

        return new FlatFileItemReaderBuilder<TwoLineDto>()
                .name("csvToJpaJob3_reader")
                .resource(new FileSystemResource(inFileName))
                .delimited().delimiter(":")
                .names("one", "two")
                .targetType(TwoLineDto.class)
                .recordSeparatorPolicy(new SimpleRecordSeparatorPolicy() {
                    @Override
                    public String postProcess(String record) {
                        if(!record.contains(":")) {
                            return null;
                        }
                        return record.trim();
                    }
                })
                .build();
    }

    @Bean
    public ItemProcessor<TwoLineDto, Two> csvToJpaJob3_processor() {
        return twoLineDto -> new Two(twoLineDto.getOne(), twoLineDto.getTwo());
    }

    @Bean
    public JpaItemWriter<Two> csvToJpaJob3_dbItemWriter() {
        JpaItemWriter<Two> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
