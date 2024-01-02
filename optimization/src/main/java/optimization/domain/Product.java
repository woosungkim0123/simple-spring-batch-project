package optimization.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "product", indexes = {
        @Index(name = "idx_product_create_date", columnList = "createDate")
})
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;

    private long amount;

    private LocalDate createDate;

    @Builder
    public Product(Long id, String name, long amount, LocalDate createDate) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.createDate = createDate;
    }
}
