package app.dtos;

import app.entities.Gender;

public record PersonDto(
        Integer id,
        String name,
        Gender gender,
        Double popularity,
        String job,
        String character) {
}