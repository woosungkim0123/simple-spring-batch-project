package optimization.reader;

import jakarta.persistence.EntityManagerFactory;
import optimization.domain.Product;
import optimization.domain.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static optimization.domain.QProduct.product;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBatchTest
@SpringBootTest
class QuerydslPagingItemReaderTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @DisplayName("QuerydslPagingItemReader가 정상적으로 값을 반환한다.")
    @Test
    void reader_returns_value_successfully() throws Exception {
        // given
        LocalDate date = LocalDate.of(2023, 12, 23);

        productRepository.save(Product.builder().name("test").amount(1000).createDate(date).build());
        productRepository.save(Product.builder().name("test").amount(2000).createDate(date).build());

        /**
         * pageSize = 1, 2개의 데이터, 총 3번의 페이징 쿼리 발생
         * - select p1_0.id,p1_0.amount ... from product p1_0 where p1_0.create_date=? offset ? rows fetch first ? rows only
         * - select p1_0.id,p1_0.amount ... from product p1_0 where p1_0.create_date=? offset ? rows fetch first ? rows only
         * - select p1_0.id,p1_0.amount ... from product p1_0 where p1_0.create_date=? offset ? rows fetch first ? rows only
         */
        int pageSize = 1;

        QuerydslPagingItemReader<Product> reader = new QuerydslPagingItemReader<>(entityManagerFactory, pageSize, queryFactory -> queryFactory
                .selectFrom(product)
                .where(product.createDate.eq(date)));

        reader.open(new ExecutionContext()); // reader만 단독으로 테스트 하기 위해서 별도의 실행환경을 등록해야하고, open을 해주지 않으면 EntityManager가 등록되지 않는다.

        // when
        Product read1 = reader.read();
        Product read2 = reader.read();
        Product read3 = reader.read();

        // then
        assertThat(read1.getAmount()).isEqualTo(1000);
        assertThat(read2.getAmount()).isEqualTo(2000);
        assertThat(read3).isNull();
    }

    @DisplayName("빈값인 경우 null이 반환된다.")
    @Test
    void reader_returns_null_when_empty() throws Exception {
        // given
        LocalDate date = LocalDate.of(2023, 12, 23);

        int pageSize = 1;

        QuerydslPagingItemReader<Product> reader = new QuerydslPagingItemReader<>(entityManagerFactory, pageSize, queryFactory -> queryFactory
                .selectFrom(product)
                .where(product.createDate.eq(date)));

        reader.open(new ExecutionContext());

        // when
        Product read1 = reader.read();

        // then
        assertThat(read1).isNull();
    }
}