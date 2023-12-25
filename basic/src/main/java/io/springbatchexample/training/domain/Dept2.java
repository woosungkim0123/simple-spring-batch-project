package io.springbatchexample.training.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@ToString
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
public class Dept2 {

    @Id
    private Long id;

    private String name;

    private String location;

    public Dept2(Long id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }
}
