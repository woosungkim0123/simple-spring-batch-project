package io.springbatchexample.twodb.batch;


import io.springbatchexample.twodb.db1.Dept3;
import io.springbatchexample.twodb.db2.Dept4;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * db1 -> db2 저장하기
 */
@Slf4j
//@Configuration
public class JpaPageJob3 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory primaryEntityManagerFactory;
    private final DataSource secondDatasource;
    private final EntityManagerFactory secondaryEntityManagerFactory;
    private final PlatformTransactionManager secondTransactionManager;

    public JpaPageJob3(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory primaryEntityManagerFactory, @Qualifier("secondDatasource") DataSource secondDatasource, @Qualifier("secondEntityManagerFactory") EntityManagerFactory secondaryEntityManagerFactory, @Qualifier("secondTransactionManager") PlatformTransactionManager secondTransactionManager) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.primaryEntityManagerFactory = primaryEntityManagerFactory;
        this.secondDatasource = secondDatasource;
        this.secondaryEntityManagerFactory = secondaryEntityManagerFactory;
        this.secondTransactionManager = secondTransactionManager;
    }

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
                .writer(jpaPageJob3_writer())
                .transactionManager(secondTransactionManager) // JPA 필수
                .build();
    }

    @Bean
    @JobScope
    public JpaPagingItemReader<Dept3> jpaPageJob3_dbItemReader() {
        return new JpaPagingItemReaderBuilder<Dept3>()
                .name("jpaPageJob3_dbItemReader")
                .entityManagerFactory(primaryEntityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM Dept3 d ORDER BY d.id asc") // 정렬조건 필수
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Dept3, Dept4> jpaPageJob3_processor() {
        return dept -> new Dept4(dept.getId(), "NEW_" + dept.getName(), "NEW_" + dept.getLocation());
    }

    /**
     * JPA 사용해서 처리 (트랜잭션 필요)
     */
    @Bean
    @StepScope
    public ItemWriter<Dept4> jpaPageJob3_writer() {
        JpaItemWriter<Dept4> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(secondaryEntityManagerFactory);
        return jpaItemWriter;
    }

    /**
     * JDBC 사용해서 처리
     */
    @Bean
    @StepScope
    public JdbcBatchItemWriter<Dept4> jdbcBatchItemWriter() {
        return new JdbcBatchItemWriterBuilder<Dept4>()
                .dataSource(secondDatasource)
                .sql("INSERT INTO dept4(id, name, location) VALUES (:id, :name, :location)")
                .beanMapped()
                .build();
    }
}
