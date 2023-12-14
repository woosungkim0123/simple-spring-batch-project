package n_1.domain.set;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class ProductSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private long price;

    @ManyToOne
    @JoinColumn(name = "store_set_id")
    private StoreSet storeSet;

    public ProductSet(String name, long price) {
        this.name = name;
        this.price = price;
    }

    public void updateStore(StoreSet storeSet){
        this.storeSet = storeSet;
    }
}