package app.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(length = 1000)
    private String overview;

    private boolean adult;
    private String originalLanguage;
    private double popularity;
    private LocalDate releaseDate;


    @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "movie_director",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "director_id")
    )
    private List<Director> directors;


    @OneToMany(mappedBy = "movie", fetch = FetchType.EAGER)
    private Set<JoinMovieActor> joins = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    private List<Genre> genres;
}