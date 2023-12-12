package n_1.domain.list_change_set_domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;

@NoArgsConstructor
@Getter
@Entity
public class StoreSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;

    @OneToMany(mappedBy = "storeSet", cascade = ALL)
    private Set<ProductSet> products = new LinkedHashSet<>();

    @OneToMany(mappedBy = "storeSet", cascade = ALL)
    private Set<EmployeeSet> employees = new LinkedHashSet<>();

    public StoreSet(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public void addProduct(ProductSet product){
        this.products.add(product);
        product.updateStore(this);
    }

    public void addEmployee(EmployeeSet employee){
        this.employees.add(employee);
        employee.updateStore(this);
    }
}