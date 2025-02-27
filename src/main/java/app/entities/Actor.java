package app.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

//@EqualsAndHashCode
@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Actor {
    @Id
    private Integer id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private double popularity;

    @OneToMany(mappedBy = "actor")
    private Set<JoinMovieActor> joins = new HashSet<>();
}