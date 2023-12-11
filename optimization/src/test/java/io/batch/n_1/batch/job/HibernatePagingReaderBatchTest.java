package io.batch.n_1.batch.job;

import io.batch.n_1.mock.MockStoreData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HibernatePagingItemReader는 문제가 발생하지 않음. (batch size 사용으로 in query 사용)
 * 맨 밑 주석에서 로그 확인 가능
 */
@TestPropertySource(properties = "job.name=hibernatePagingReaderBatch")
@SpringBootTest
class HibernatePagingReaderBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private MockStoreData mockStoreData;

    @DisplayName("HibernatePagingItemReader는 in query를 사용하는 걸 보여주는 테스트")
    @Test
    public void HibernatePagingItemReader_used_in_query() throws Exception {
        // given
        mockStoreData.saveStores();

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("address", "서울")
                .addDate("requestDate", new Date())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}

/**
 * Hibernate: select store0_.id as id1_3_, store0_.address as address2_3_, store0_.name as name3_3_ from store store0_ where store0_.address like ? limit ?
 * Hibernate: select products0_.store_id as store_id4_2_1_, products0_.id as id1_2_1_, products0_.id as id1_2_0_, products0_.name as name2_2_0_, products0_.price as price3_2_0_, products0_.store_id as store_id4_2_0_ from product products0_ where products0_.store_id in (?, ?, ?)
 * Hibernate: select employees0_.store_id as store_id4_0_1_, employees0_.id as id1_0_1_, employees0_.id as id1_0_0_, employees0_.hire_date as hire_dat2_0_0_, employees0_.name as name3_0_0_, employees0_.store_id as store_id4_0_0_ from employee employees0_ where employees0_.store_id in (?, ?, ?)
 */