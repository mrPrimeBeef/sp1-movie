package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer tmdbId;
    private String title;
    private String originalTitle;
    private String overview;
    private boolean adult;
    private String originalLanguage;
    private double popularity;
    private LocalDate releaseDate;

    @ManyToMany
    private List<Director> directors;

    @OneToMany(mappedBy = "movie")
    private Set<JoinMovieActor> joins = new HashSet<>();

    @ManyToMany
    private List<Genre> genres;
}