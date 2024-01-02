package optimization.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class ProductBackup {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;

    private long amount;

    private LocalDate createDate;

    @Builder
    public ProductBackup(Long id, String name, long amount, LocalDate createDate) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.createDate = createDate;
    }
}
