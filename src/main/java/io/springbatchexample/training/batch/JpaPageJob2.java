package io.springbatchexample.training.batch;

import io.springbatchexample.training.domain.Dept1;
import io.springbatchexample.training.domain.Dept2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

/**
 * dept1 데이터를 읽어서 가공후(process) dept2에 저장하는 배치
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob2 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private static final int chunkSize = 10;

    @Bean
    public Job jpaPageJob2_batchBuild() {
        return jobBuilderFactory.get("JpaPageJob2")
                .start(jpaPageJob2_step1())
                .build();
    }

    @Bean
    public Step jpaPageJob2_step1() {
        return stepBuilderFactory.get("JpaPageJob2_step1")
                .<Dept1, Dept2>chunk(chunkSize)
                .reader(jpaPageJob2_dbItemReader())
                .processor(jpaPageJob2_processor())
                .writer(jpaPageJob2_dbItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Dept1> jpaPageJob2_dbItemReader() {
        return new JpaPagingItemReaderBuilder<Dept1>()
                .name("jpaPageJob2_dbItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM Dept1 d ORDER BY d.id asc")
                .build();
    }

    private ItemProcessor<Dept1, Dept2> jpaPageJob2_processor() {
        return dept -> new Dept2(dept.getId(), "NEW_" + dept.getName(), "NEW_" + dept.getLocation());
    }

    @Bean
    public JpaItemWriter<Dept2> jpaPageJob2_dbItemWriter() {
        JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
