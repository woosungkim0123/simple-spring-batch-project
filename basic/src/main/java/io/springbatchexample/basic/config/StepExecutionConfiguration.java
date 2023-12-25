package io.springbatchexample.basic.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1. 두번째 STEP에서 예외 발생
 * JOB FAIL -> 재실행 가능
 * STEP1 COMPLETE -> 재실행 불가능
 * STEP2 FAIL -> 재실행 됨
 * STEP3 NOT EXECUTE -> 재실행 됨
 *
 * 2. 정상적으로 모두 진행
 * JOB COMPLETE -> 재실행 불가능
 * STEP2 COMPLETE -> 재실행 불가능
 * STEP3 COMPLETE -> 재실행 불가능
 */
@RequiredArgsConstructor
//@Configuration
public class StepExecutionConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(step1())
                .next(step2())
                .next(step3())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step 1");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step 2");
                    //throw new RuntimeException("step 2 failed");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step 3");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
