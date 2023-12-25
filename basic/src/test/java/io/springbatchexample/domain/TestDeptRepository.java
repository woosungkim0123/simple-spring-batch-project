package io.springbatchexample.domain;

import io.springbatchexample.training.domain.Dept1;
import io.springbatchexample.training.domain.Dept1Repository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

@SpringBootTest
public class TestDeptRepository {

    @Autowired
    private Dept1Repository dept1Repository;

    @Commit
    @DisplayName("training을 위한 테스트 데이터를 만든다.")
    @Test
    void depth01() {
        for (int i = 0; i < 10000; i++) {
            dept1Repository.save(new Dept1((long) i, "name_" + i, "location_" + i));
        }
    }
}
