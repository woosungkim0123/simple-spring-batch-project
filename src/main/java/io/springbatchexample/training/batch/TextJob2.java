package io.springbatchexample.training.batch;

import io.springbatchexample.training.custom.CustomPassThroughLineAggregator;
import io.springbatchexample.training.dto.OneLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

/**
 * 파일을 한줄씩 읽어 특정한 형식의 데이터를 변형 후 파일 형태로 만드는 배치
 *
 * ps. 커스텀 할때는 processor를 이용해서 하는게 좋음
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class TextJob2 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private int chunkSize = 5;

    @Bean
    public Job textJob2_batchBuild() {
        return jobBuilderFactory.get("textJob2")
                .start(textJob2_batchStep1())
                .build();
    }

    @Bean
    public Step textJob2_batchStep1() {
        return stepBuilderFactory.get("textJob2_batchStep1")
                .<OneLineDto, OneLineDto>chunk(chunkSize)
                .reader(textJob2_FileReader())
                .writer(textJob2_FileWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<OneLineDto> textJob2_FileReader() {
        FlatFileItemReader<OneLineDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/textJob2_input.txt"));
        flatFileItemReader.setLineMapper((line, lineNumber) -> new OneLineDto(lineNumber + "_" + line));
        return flatFileItemReader;
    }

    @Bean
    public FlatFileItemWriter<OneLineDto> textJob2_FileWriter() {
        return new FlatFileItemWriterBuilder<OneLineDto>()
                .name("textJob2_FileWriter")
                .resource(new FileSystemResource("output/textJob2_output.txt"))
                .lineAggregator(new CustomPassThroughLineAggregator<>())
                .build();
    }
}
