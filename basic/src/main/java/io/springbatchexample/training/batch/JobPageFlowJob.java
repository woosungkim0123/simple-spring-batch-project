package io.springbatchexample.training.batch;


import io.springbatchexample.training.domain.Dept1;
import io.springbatchexample.training.domain.Dept2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;

/**
 * 병렬 처리
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class JobPageFlowJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private final int chunkSize = 10;

    @Bean
    public Job jpaPageFlowJob_batchBuild() {
        Flow flow1 = new FlowBuilder<Flow>("flow1")
                .start(jpaPageJob_1_batchStep1())
                .build();

        Flow flow2 = new FlowBuilder<Flow>("flow2")
                .start(jpaPageJob_2_batchStep1())
                .build();

        Flow parallelStepsFlow = new FlowBuilder<Flow>("parallelStepsFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(flow1, flow2)
                .build();

        return jobBuilderFactory.get("jpaPageFlowJob")
                .start(parallelStepsFlow)
                .build().build();
    }

    @Bean
    public Step jpaPageJob_1_batchStep1() {
        return stepBuilderFactory.get("jpaPageJob_1_batchStep1")
                .<Dept1, Dept2>chunk(chunkSize)
                .reader(jpaPageFlowJob_1_dbItemReader())
                .processor(jpaPageFlowJob_1_processor())
                .writer(jpaPageFlowJob_1_dbItemWriter())
                .build();
    }

    @Bean
    public Step jpaPageJob_2_batchStep1() {
        return stepBuilderFactory.get("jpaPageJob_2_batchStep1")
                .<Dept1, Dept2>chunk(chunkSize)
                .reader(jpaPageFlowJob_2_dbItemReader())
                .processor(jpaPageFlowJob_2_processor())
                .writer(jpaPageFlowJob_2_dbItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Dept1> jpaPageFlowJob_1_dbItemReader() {
        return new JpaPagingItemReaderBuilder<Dept1>()
                .name("jpaPageFlowJob_dbItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM Dept1 d WHERE d.id <= 5000 ORDER BY d.id asc")
                .build();
    }

    @Bean
    public ItemProcessor<Dept1, Dept2> jpaPageFlowJob_1_processor() {
        return dept1 -> new Dept2(dept1.getId(), "NEW_"  + dept1.getName(), "NEW_"  + dept1.getLocation());
    }

    @Bean
    public JpaItemWriter<Dept2> jpaPageFlowJob_1_dbItemWriter() {
        JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    @Bean
    public JpaPagingItemReader<Dept1> jpaPageFlowJob_2_dbItemReader() {
        return new JpaPagingItemReaderBuilder<Dept1>()
                .name("jpaPageFlowJob_dbItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM Dept1 d WHERE d.id > 5000 ORDER BY d.id asc")
                .build();
    }

    @Bean
    public ItemProcessor<Dept1, Dept2> jpaPageFlowJob_2_processor() {
        return dept1 -> new Dept2(dept1.getId(), "NEW_"  + dept1.getName(), "NEW_"  + dept1.getLocation());
    }

    @Bean
    public JpaItemWriter<Dept2> jpaPageFlowJob_2_dbItemWriter() {
        JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
