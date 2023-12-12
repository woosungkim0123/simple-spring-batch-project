package n_1.batch.job;

import n_1.mock.MockStoreData;
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
 * List -> Set으로 변경해 fetch join을 사용할 수 있도록 하였지만 메모리 관련 warning이 발생하는 테스트
 *
 * 로그 결과 :: paging 쿼리가 없고 warning 발생
 * WARN o.h.h.internal.ast.QueryTranslatorImpl: HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!
 * Hibernate: select ... from store_set storeset0_ 
 * inner join product_set products1_ on storeset0_.id=products1_.store_set_id 
 * inner join employee_set employees2_ on storeset0_.id=employees2_.store_set_id where storeset0_.address like ? 
 */
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.default_batch_fetch_size=0",
        "job.name=customJpaPagingFetchReaderBatchJob"
})
@SpringBootTest
class CustomJpaPagingFetchReaderBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private MockStoreData mockStoreData;

    @DisplayName("Set을 사용하여 fetch join을 사용할 수 있지만 메모리 관련 warning이 발생한다.")
    @Test
    public void batch_with_fetch_join_enabled_using_set() throws Exception {
        // given
        mockStoreData.saveStoresSet();

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