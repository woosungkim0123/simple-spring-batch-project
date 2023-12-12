package n_1.domain.list_change_set_domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Entity
public class EmployeeSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate hireDate;

    @ManyToOne
    @JoinColumn(name = "store_set_id", foreignKey = @ForeignKey(name = "FK_EMPLOYEE_STORE_SET"))
    private StoreSet storeSet;

    public EmployeeSet(String name, LocalDate hireDate) {
        this.name = name;
        this.hireDate = hireDate;
    }

    public void updateStore(StoreSet storeSet){
        this.storeSet = storeSet;
    }
}
