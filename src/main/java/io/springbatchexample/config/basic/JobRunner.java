package io.springbatchexample.config.basic;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;


@RequiredArgsConstructor
@Component
public class JobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job job;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String version = args.containsOption("version")
                ? args.getOptionValues("version").get(0)
                : "1";

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("version", version)
                .addLong("seq", 1L)
                //.addDate("time", new Date())
                .addDouble("percent", 77.0)
                .toJobParameters();

        jobLauncher.run(job, jobParameters);

    }
}
