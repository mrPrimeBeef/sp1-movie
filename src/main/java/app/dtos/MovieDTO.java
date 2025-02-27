package app.dtos;

import java.time.LocalDate;
import java.util.List;

public record MovieDTO(
        Integer id,
        Integer tmdbId,
        String title,
        String originalTitle,
        String overview,
        boolean adult,
        String originalLanguage,
        double popularity,
        LocalDate releaseDate,
        List<DirectorDTO> directors,
        List<GenreDTO> genres,
        List<MovieActorDTO> actors
) {}