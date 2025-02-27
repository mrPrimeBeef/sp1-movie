package app.dtos;

import app.entities.Gender;

public record DirectorDTO(
        String name,
        Gender gender,
        double popularity,
        String tmdbId
) {}
