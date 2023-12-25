package io.springbatchexample.training.batch;

import io.springbatchexample.training.domain.Dept1;
import io.springbatchexample.training.domain.Dept2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

/**
 * 10000 data :: Chunk size Test batch
 * 1. chunk size : 10, time : 44s 776ms
 * 2. chunk size : 100, time : 34s 235ms
 * 3. chunk size : 1000, time : 31s 391ms
 * 4. chunk size : 10000, time : 16s 299ms
 * -> 성능에 맞는 chunk size를 찾아야함
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob3 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job jpaPageJob3_batchBuild() {
        return jobBuilderFactory.get("jpaPageJob3")
                .start(jpaPageJob3_step1(0))
                .build();
    }

    /**
     * 파라미터가 들어가면 Scope가 반드시 정의되어야함
     */
    @Bean
    @JobScope
    public Step jpaPageJob3_step1(@Value("#{jobParameters[chunkSize]}") int chunkSize) {
        return stepBuilderFactory.get("jpaPageJob3_step1")
                .<Dept1, Dept2>chunk(chunkSize)
                .reader(jpaPageJob3_dbItemReader(chunkSize))
                .processor(jpaPageJob3_processor())
                .writer(jpaPageJob3_dbItemWriter())
                .build();
    }

    @Bean
    @JobScope
    public JpaPagingItemReader<Dept1> jpaPageJob3_dbItemReader(@Value("#{jobParameters[chunkSize]}") int chunkSize) {
        return new JpaPagingItemReaderBuilder<Dept1>()
                .name("jpaPageJob3_dbItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM Dept1 d ORDER BY d.id asc")
                .build();
    }

    private ItemProcessor<Dept1, Dept2> jpaPageJob3_processor() {
        return dept -> new Dept2(dept.getId(), "NEW_" + dept.getName(), "NEW_" + dept.getLocation());
    }

    @Bean
    public JpaItemWriter<Dept2> jpaPageJob3_dbItemWriter() {
        JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
