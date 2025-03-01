package app.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@ToString
@Getter
@NoArgsConstructor
@Entity
public class Movie {
    @Id
    private Integer id;

    private String title;
    private String originalTitle;
    private boolean adult;
    private String originalLanguage;
    private double popularity;
    private LocalDate releaseDate;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Genre> genres;

    @ToString.Exclude
    @OneToMany(mappedBy = "movie", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<Credit> credits = new HashSet<>();

    @Column(length = 1000)
    private String overview;

    public Movie(Integer id, String title, String originalTitle, boolean adult, String originalLanguage, double popularity, LocalDate releaseDate, Set<Genre> genres, String overview) {
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.adult = adult;
        this.originalLanguage = originalLanguage;
        this.popularity = popularity;
        this.releaseDate = releaseDate;
        this.genres = genres;
        this.overview = overview;
    }

    public void addCredit(Person person, String job, String character) {
        credits.add(new Credit(null, this, person, job, character));
    }

}