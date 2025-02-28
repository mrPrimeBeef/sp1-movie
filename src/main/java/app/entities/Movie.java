package app.entities;

import java.time.LocalDate;
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
    private boolean adult;
    private String originalLanguage;
    private double popularity;
    private LocalDate releaseDate;

    @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Genre> genres;

    @ToString.Exclude
    @Setter
    @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<Credit> credits;

    @Column(length = 1000)
    private String overview;

}