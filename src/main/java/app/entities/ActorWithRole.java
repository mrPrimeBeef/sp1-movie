package app.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActorWithRole {
    private final Actor actor;
    private final String character;
}