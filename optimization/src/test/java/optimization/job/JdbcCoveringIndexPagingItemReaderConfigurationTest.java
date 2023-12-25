package optimization.job;

import optimization.domain.Product;
import optimization.domain.ProductBackup;
import optimization.domain.ProductBackupRepository;
import optimization.domain.ProductRepository;
import org.junit.jupiter.api.*;
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
import java.util.ArrayList;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 해당 테스트를 위해서 application-mysql.yml에 mysql 설정을 추가해야합니다.
 * h2로 하지 않은 이유는 mysql의 커버링 인덱스 쿼리를 테스트하기 위함입니다.
 */
@ActiveProfiles("mysql")
@TestPropertySource(properties = "job.name=jdbcCoveringIndexPagingItemReaderBatchJob")
@SpringBootTest
@SpringBatchTest
class JdbcCoveringIndexPagingItemReaderConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductBackupRepository productBackupRepository;

    public static final DateTimeFormatter FORMATTER = ofPattern("yyyy-MM-dd");

    @BeforeEach
    public void deleteAllData() {
        productRepository.deleteAll();
        productBackupRepository.deleteAll();
    }

    @DisplayName("부분적으로 적용되는 커버링 인덱스 쿼리를 사용하기위해 JDBC Reader를 사용하여 배치가 정상적으로 실행된다.")
    @Test
    void batch_success_using_jdbc_reader_and_covering_index() throws Exception {
        // given
        saveMockProductData();

        LocalDate date = LocalDate.of(2023, 12, 25);

        JobParameters jobParameters = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
                .addString("date", date.format(FORMATTER))
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        List<ProductBackup> productBackupList = productBackupRepository.findAll();
        assertThat(productBackupList.size()).isEqualTo(3);
        assertThat(productBackupList.get(0).getAmount()).isEqualTo(7000);
        assertThat(productBackupList.get(1).getAmount()).isEqualTo(8000);
        assertThat(productBackupList.get(2).getAmount()).isEqualTo(9000);
    }

    private void saveMockProductData() {
        LocalDate[] dates = {
                LocalDate.of(2023, 12, 23),
                LocalDate.of(2023, 12, 24),
                LocalDate.of(2023, 12, 25)
        };
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            LocalDate date = dates[i / 3]; // 3개의 제품마다 날짜가 바뀝니다.
            int amount = (i + 1) * 1000;
            products.add(Product.builder().name("product" + (i + 1)).amount(amount).createDate(date).build());
        }

        productRepository.saveAll(products);
    }
}