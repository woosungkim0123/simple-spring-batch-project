package io.springbatchexample.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
public class JobParameterConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        // 1
                        JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
                        String version = jobParameters.getString("version");
                        Long seq = jobParameters.getLong("seq");
                        Double percent = jobParameters.getDouble("percent");
                        Date time = jobParameters.getDate("time");
                        System.out.println("time = " + time);
                        System.out.println("version = " + version);
                        System.out.println("seq = " + seq);
                        System.out.println("percent = " + percent);

                        // 2
                        Map<String, Object> jobParameters1 = chunkContext.getStepContext().getJobParameters();

                        System.out.println("step 1");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step 2");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
