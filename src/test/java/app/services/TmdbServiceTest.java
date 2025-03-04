package app.services;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import app.dtos.CreditDto;
import app.dtos.GenreDto;
import app.enums.Gender;
import app.dtos.MovieDto;
import app.utils.Utils;

public class TmdbServiceTest {

    Boolean TEST_API_REQUESTS = Boolean.parseBoolean(Utils.getPropertyValue("TEST_API_REQUESTS", "config.properties"));

    private static final int MAX_REQUESTS_PER_SECOND = 30;
    private static final long DELAY_MILLISECONDS = 1000 / MAX_REQUESTS_PER_SECOND;

    @Test
    void getGenres() {

        if (TEST_API_REQUESTS) {

            Set<GenreDto> genreDtos = TmdbService.getGenres();

            GenreDto genreDto = genreDtos.stream()
                    .filter(g -> g.id() == 99)
                    .findFirst()
                    .orElse(null);

            assertEquals("Documentary", genreDto.name());

        }

    }


    @Test
    void getDanishMoviesSince2020() {

        if (TEST_API_REQUESTS) {

            Set<MovieDto> movieDtos = TmdbService.getDanishMoviesSince2020(DELAY_MILLISECONDS);
            System.out.println(movieDtos.size());
            assertTrue(movieDtos.size() >= 1294);

            MovieDto movieDto = movieDtos.stream()
                    .filter(m -> m.id() == 1275299)
                    .findFirst()
                    .orElse(null);

            assertEquals("Badabing og Bang - Hurra, årtiet er slut!", movieDto.title());
            assertEquals(LocalDate.of(2020, 1, 1), movieDto.releaseDate());

        }

    }

    @Test
    void getCreditsForMovie() {

        if (TEST_API_REQUESTS) {

            Set<CreditDto> creditDtos = TmdbService.getCreditsForMovie(659940);

            CreditDto creditDto = creditDtos.stream()
                    .filter(c -> c.personId() == 4455)
                    .findFirst()
                    .orElse(null);

            assertEquals("Ulrich Thomsen", creditDto.name());
            assertEquals(Gender.MAN, creditDto.gender());

        }

    }


}