package app.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(of = "id")
@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Movie {
    @Id
    private Integer id;

    private String title;
    private String originalTitle;

    @Column(length = 1000)
    private String overview;

    private boolean adult;
    private String originalLanguage;
    private double popularity;
    private LocalDate releaseDate;

    @ManyToMany
    private List<Genre> genres;

    @Setter
    @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST)
    private Set<Credit> credits = new HashSet<>();

}