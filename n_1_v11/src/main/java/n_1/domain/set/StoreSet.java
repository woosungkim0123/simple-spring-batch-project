package n_1.domain.set;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Entity
public class StoreSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;

    @OneToMany(mappedBy = "storeSet")
    private Set<ProductSet> products = new LinkedHashSet<>();

    @OneToMany(mappedBy = "storeSet")
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