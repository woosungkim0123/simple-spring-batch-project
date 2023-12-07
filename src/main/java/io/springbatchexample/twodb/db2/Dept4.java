package io.springbatchexample.twodb.db2;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@ToString
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "dept4")
@Entity
public class Dept4 {

    @Id
    private Long id;

    private String name;

    private String location;

    public Dept4(Long id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }
}
