package app.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode
@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Director {
    @Id
    private Integer id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private double popularity;

    @ManyToMany(mappedBy = "directors")
    private List<Movie> movies;
}