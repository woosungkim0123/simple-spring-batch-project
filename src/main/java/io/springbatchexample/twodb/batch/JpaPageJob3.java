package io.springbatchexample.twodb.batch;


import io.springbatchexample.twodb.db1.Dept3;
import io.springbatchexample.twodb.db2.Dept4;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * db1 -> db2 저장하기
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob3 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory primaryEntityManagerFactory;

    @Qualifier("secondEntityManagerFactory")
    private final EntityManagerFactory secondEntityManagerFactory;
    @Qualifier("secondDatasource")
    private final DataSource secondDatasource;


    private static final int chunkSize = 10;

    @Bean
    public Job jpaPageJob3_batchBuild() {
        return jobBuilderFactory.get("JpaPageJob3")
                .start(jpaPageJob3_step1())
                .build();
    }

    @Bean
    public Step jpaPageJob3_step1() {
        return stepBuilderFactory.get("JpaPageJob3_step1")
                .<Dept3, Dept4>chunk(chunkSize)
                .reader(jpaPageJob3_dbItemReader())
                .processor(jpaPageJob3_processor())
                .writer(jpaPageJob3_dbItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Dept3> jpaPageJob3_dbItemReader() {
        return new JpaPagingItemReaderBuilder<Dept3>()
                .name("jpaPageJob3_dbItemReader")
                .entityManagerFactory(primaryEntityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM Dept3 d ORDER BY d.id asc") // 정렬조건 필수
                .build();
    }

    @Bean
    public ItemProcessor<Dept3, Dept4> jpaPageJob3_processor() {
        return dept -> new Dept4(dept.getId(), "NEW_" + dept.getName(), "NEW_" + dept.getLocation());
    }

    @Bean
    public JpaItemWriter<Dept4> jpaPageJob3_dbItemWriter() {
        JpaItemWriter<Dept4> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(secondEntityManagerFactory);
        return jpaItemWriter;
    }
}
