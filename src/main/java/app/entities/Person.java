package app.entities;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

//@EqualsAndHashCode
@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Person {
    @Id
    private Integer id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private double popularity;

    @ToString.Exclude
    @OneToMany(mappedBy = "person", fetch=FetchType.LAZY)
    private Set<Credit> credits;
}