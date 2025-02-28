package app.dtos;

import app.entities.Gender;

public record MemberDto(
        Integer id,
        String name,
        Gender gender,
        Double popularity,
        String job,
        String character) {
}