package app.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.enums.Gender;

public record CreditDto(
        @JsonProperty("id")
        Integer personId,
        String name,
        Gender gender,
        Double popularity,
        String job,
        String character) {
}