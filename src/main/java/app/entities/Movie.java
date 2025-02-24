package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String originalTitle;
    private String overview;
    private boolean adult;
    private String originalLanguage;
    private double popularity;
    private String releaseDate;

    @ManyToMany
    private List<Actor> actors;

    @ManyToMany
    private List<Director> directors;

    @OneToMany(mappedBy = "movie")
    private Set<JoinMovieActor> joins = new HashSet<>();

    @ElementCollection(targetClass = Genre.class) //  JPA laver en separat tabel for ENUM-lister.
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "movie_genre", joinColumns = @JoinColumn(name = "movie_id")) // Opretter en tabel movie_genre med en foreign key til movie_id.Tabellen indeholder en liste af Genre-værdier for hver film.
    @Column(name = "genre") // Angiver kolonnen, hvor ENUM-værdierne gemmes.
    private List<Genre> genres;
}