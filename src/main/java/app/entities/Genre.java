package app.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Genre {
    @Id
    Integer id;
    String name;
}