package app.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Builder
@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Genree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer jpaId;
    Integer id;
    String name;
}