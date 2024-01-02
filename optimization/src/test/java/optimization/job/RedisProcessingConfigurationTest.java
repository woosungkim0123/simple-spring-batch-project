package optimization.job;

import optimization.domain.Product;
import optimization.domain.ProductBackup;
import optimization.domain.ProductBackupRepository;
import optimization.domain.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@TestPropertySource(properties = "job.name=redisProcessingBatchJob")
@SpringBootTest
@SpringBatchTest
class RedisProcessingConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductBackupRepository productBackupRepository;

    public static final DateTimeFormatter FORMATTER = ofPattern("yyyy-MM-dd");

    @DisplayName("들어온 날짜보다 크거나 같은 날짜의 데이터를 Redis에 저장하고, 해당 날짜의 데이터를 합산하여 저장한다.")
    @Test
    void batch_success_using_jdbc_reader_and_covering_index() throws Exception {
        // given
        LocalDate date1 = LocalDate.of(2023, 12, 23);
        LocalDate date2 = LocalDate.of(2023, 12, 24);

        productRepository.save(Product.builder().name("product1").amount(1000).createDate(date1).build());
        productRepository.save(Product.builder().name("product2").amount(2000).createDate(date1).build());
        productRepository.save(Product.builder().name("product3").amount(3000).createDate(date2).build());

        JobParameters jobParameters = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
                .addString("date", date1.format(FORMATTER))
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        List<ProductBackup> productBackupList = productBackupRepository.findAll();
        assertThat(productBackupList.size()).isEqualTo(2);
        assertThat(productBackupList.get(0).getAmount()).isEqualTo(3000);
        assertThat(productBackupList.get(1).getAmount()).isEqualTo(3000);
    }
}