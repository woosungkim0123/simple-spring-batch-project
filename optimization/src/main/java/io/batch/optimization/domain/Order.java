package io.batch.optimization.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Table(name = "orders")
@Entity
public class Order {

    @Id
    private Long id;
}
