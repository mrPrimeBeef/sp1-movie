package app.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@ToString
@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Director {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    String tmdbId;

    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private double popularity;

    @ToString.Exclude
    @ManyToMany(mappedBy = "directors", fetch = FetchType.EAGER)
    private List<Movie> movies;
}