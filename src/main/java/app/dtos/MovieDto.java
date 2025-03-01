package app.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Set;

public record MovieDto(Integer id,
                       String title,
                       @JsonProperty("original_title")
                       String originalTitle,
                       Boolean adult,
                       @JsonProperty("original_language")
                       String originalLanguage,
                       Double popularity,
                       @JsonProperty("release_date")
                       LocalDate releaseDate,
                       @JsonProperty("genre_ids")
                       Set<Integer> genreIds,
                       String overview
) {
}