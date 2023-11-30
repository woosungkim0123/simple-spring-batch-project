package io.springbatchexample.training.batch;

import io.springbatchexample.training.domain.Dept1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob1 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private int chunkSize = 10; // chunk :단위, 덩어리
    // 한꺼번에 batch를 많이 하게되면 메모리를 모두 사용할 수 있으니 잘라서 순차적으로 배치를 돌림.
    // 반드시 맞는건 아니고 배치 특성에 따라 한번에 메모리에 가져와야하는 경우도 있음. (잘라서 가져오면 가지고 오는 동안 배치 내용이 달라질 수 있음)

    @Bean
    public Job jpaPageJob1_batchBuild() {
        return jobBuilderFactory.get("JpaPageJob1")
                .start(jpaPageJob1_step1())
                .build();
    }

    @Bean
    public Step jpaPageJob1_step1() {
        return stepBuilderFactory.get("JpaPageJob1_step1")
                .<Dept1, Dept1>chunk(chunkSize) // <INPUT, OUTPUT>
                .reader(jpaPageJob1_dbItemReader()) // 3개가 있는데 가운데 process 넣어도되고 안넣어도되고
                .writer(jpaPageJob1_printItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Dept1> jpaPageJob1_dbItemReader() {
        return new JpaPagingItemReaderBuilder<Dept1>()
                .name("jpaPageJob1_dbItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM Dept1 d ORDER BY d.id asc")
                .build();
    }

    @Bean
    public ItemWriter<Dept1> jpaPageJob1_printItemWriter() {
        return list -> {
            for (Dept1 dept1 : list) {
                log.info("Dept1 : {}", dept1.toString());
            }
        };
    }
}
