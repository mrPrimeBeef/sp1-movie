package app.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

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
                       List<Integer> genreIds,
                       String overview
) {
}