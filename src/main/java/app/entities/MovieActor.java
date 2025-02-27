package app.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MovieActor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //    @JoinColumn(name = "movie_id", nullable = false)
    @ManyToOne
    private Movie movie;


    //    @JoinColumn(name = "actor_id", nullable = false)
    @ManyToOne
    private Actor actor;

    private String character;
}