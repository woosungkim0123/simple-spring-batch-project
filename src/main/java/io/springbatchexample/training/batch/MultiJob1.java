package io.springbatchexample.training.batch;

import io.springbatchexample.training.dto.TwoLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

/**
 * 빈 생성 -> 배치 기동 - 배치 파라미터를 못가져옴
 * @JobScope, @StepScope를 사용하면 빈 생성 시점이 아닌 실행 시점에 빈을 생성한다.
 *
 * --job.name=multiJob1 inFileName=multiJob1_input.txt version=1 outFileName=multiJob1_output.txt
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class MultiJob1 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private static final int chunkSize = 5;

    @Bean
    public Job multiJob1_batchBuild() {
        return jobBuilderFactory.get("multiJob1")
                .start(multiJob1_batchStep1(null))
                .build();
    }

    @JobScope
    @Bean
    public Step multiJob1_batchStep1(@Value("#{jobParameters[version]}") String version) {

        log.info("version: {}", version);

        return stepBuilderFactory.get("multiJob1_batchStep1")
                .<TwoLineDto, TwoLineDto>chunk(chunkSize)
                .reader(multiJob1_reader(null))
                .processor(multiJob1_processor(null))
                .writer(multiJob1_writer(null))
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<TwoLineDto> multiJob1_reader(@Value("#{jobParameters[inFileName]}") String inFileName) {
        return new FlatFileItemReaderBuilder<TwoLineDto>()
                .name("multiJob1_Reader")
                .resource(new ClassPathResource("sample/" + inFileName))
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

    @StepScope
    @Bean
    public ItemProcessor<TwoLineDto, TwoLineDto> multiJob1_processor(@Value("#{jobParameters[version]}") String version) {
        log.info("processor_version: {}", version);
        return twoLineDto -> new TwoLineDto(twoLineDto.getOne(), twoLineDto.getTwo());
    }

    @StepScope
    @Bean
    public FlatFileItemWriter<TwoLineDto> multiJob1_writer(@Value("#{jobParameters[outFileName]}") String outFileName) {
        return new FlatFileItemWriterBuilder<TwoLineDto>()
                .name("multiJob1_writer")
                .resource(new FileSystemResource("sample/" + outFileName))
                .lineAggregator(item -> item.getOne() + " --- " + item.getTwo())
                .build();
    }
}
