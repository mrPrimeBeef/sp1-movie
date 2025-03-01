package app.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.entities.Gender;

public record CreditDto(
        @JsonProperty("id")
        Integer personId,
        String name,
        Gender gender,
        Double popularity,
        String job,
        String character) {
}