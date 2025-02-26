package app.entities;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(of = "id")
@ToString
@Getter
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

}