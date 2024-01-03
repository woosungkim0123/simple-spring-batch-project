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
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "job.name=querydslZeroPagingItemReaderBatchJob")
@SpringBootTest
@SpringBatchTest
class QuerydslZeroPagingItemReaderConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductBackupRepository productBackupRepository;

    public static final DateTimeFormatter FORMATTER = ofPattern("yyyy-MM-dd");

    @DisplayName("QuerydslZeroPagingItemReader를 사용한 배치가 성공한다. - offset을 0으로 유지")
    @Test
    void batch_success_using_zero_offset_querydsl_reader() throws Exception {
        // given
        LocalDate date = LocalDate.of(2023, 12, 23);
        LocalDate anotherDate = LocalDate.of(2023, 12, 24);

        productRepository.save(Product.builder().name("product1").amount(1000).createDate(date).build());
        productRepository.save(Product.builder().name("product2").amount(2000).createDate(anotherDate).build());
        productRepository.save(Product.builder().name("product3").amount(3000).createDate(date).build());

        JobParameters jobParameters = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
                .addString("date", date.format(FORMATTER))
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        List<ProductBackup> productBackupList = productBackupRepository.findAll();
        assertThat(productBackupList).hasSize(2);
        assertThat(productBackupList.get(0).getName()).isEqualTo("product1_backup");
        assertThat(productBackupList.get(1).getName()).isEqualTo("product3_backup");
    }
}