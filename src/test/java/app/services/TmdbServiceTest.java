package app.services;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import app.entities.Movie;
import app.utils.Utils;

public class TmdbServiceTest {

    Boolean TEST_API_REQUESTS = Boolean.parseBoolean(Utils.getPropertyValue("TEST_API_REQUESTS", "config.properties"));

    @Test
    void getDanishMoviesSince2020() {

        if (TEST_API_REQUESTS) {

            List<Movie> movies = TmdbService.getDanishMoviesSince2020();
            assertNotNull(movies);
            assertTrue(movies.size() > 1200);

            Movie firstMovie = movies.stream()
                    .sorted(Comparator.comparing(Movie::getReleaseDate))
                    .toList().get(0);

            assertEquals(1275299, firstMovie.getTmdbId());
            assertEquals("Badabing og Bang - Hurra, årtiet er slut!", firstMovie.getTitle());
            assertEquals(LocalDate.of(2020, 1, 1), firstMovie.getReleaseDate());

        }

    }

}