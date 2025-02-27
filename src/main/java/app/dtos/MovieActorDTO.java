package app.dtos;

import app.entities.Gender;

public record MovieActorDTO(
        String name,
        Gender gender,
        double popularity,
        Integer tmdbId,
        String character
) {}
