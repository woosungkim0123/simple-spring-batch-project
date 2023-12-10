package io.batch.optimization.batch;

import io.batch.optimization.domain.Employee;
import io.batch.optimization.domain.Product;
import io.batch.optimization.domain.Store;
import io.batch.optimization.domain.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "job.name=storeBackupJob")
@SpringBootTest
class StoreBackupBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    public void testStoreInformationIsCopiedToStoreHistory() throws Exception {
        // given
        saveStoreData();
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("address", "서울")
                .addDate("requestDate", new Date())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    private void saveStoreData() {
        Store store1 = new Store("서점", "서울시 강남구");
        store1.addProduct(new Product("책1_1", 10000L));
        store1.addProduct(new Product("책1_2", 20000L));
        store1.addEmployee(new Employee("직원1", LocalDate.now()));
        storeRepository.save(store1);

        Store store2 = new Store("서점2", "서울시 강남구");
        store2.addProduct(new Product("책2_1", 10000L));
        store2.addProduct(new Product("책2_2", 20000L));
        store2.addEmployee(new Employee("직원2", LocalDate.now()));
        storeRepository.save(store2);

        Store store3 = new Store("서점3", "서울시 강남구");
        store3.addProduct(new Product("책3_1", 10000L));
        store3.addProduct(new Product("책3_2", 20000L));
        store3.addEmployee(new Employee("직원3", LocalDate.now()));
        storeRepository.save(store3);
    }
}