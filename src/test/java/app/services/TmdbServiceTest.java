package app.services;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.entities.Genre;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import app.entities.Movie;
import app.utils.Utils;

public class TmdbServiceTest {

    @Test
    void getDanishMoviesSince2020() {
        List<Genre> genres = TmdbService.getAllGenres();
        Map<Integer, Genre> genreMap = new HashMap<>();
        genres.forEach(g -> genreMap.put(g.getTmdbId(), g));

        List<Movie> movies = TmdbService.getDanishMoviesSince2020(genreMap);
        assertNotNull(movies);
        assertTrue(movies.size() > 1200);

        Movie firstMovie = movies.stream()
                .sorted(Comparator.comparing(Movie::getReleaseDate))
                .toList().get(0);

        assertEquals(1275299, firstMovie.getTmdbId());
        assertEquals("Badabing og Bang - Hurra, årtiet er slut!", firstMovie.getTitle());
        assertEquals(LocalDate.of(2020, 1, 1), firstMovie.getReleaseDate());
    }

    @Test
    void getCrewAndActorsDetails(){
        // river of blood
        TmdbService.MovieCastDTO movieCastDTO = TmdbService.getCrewAndActorsDetails("1222064");

        TmdbService.CastDTO actor1 = movieCastDTO.credits().cast().get(0);

        assertNotNull(actor1);
        assertEquals(6,movieCastDTO.credits().cast().size());
        assertEquals("Joseph Millson",actor1.name());
        assertEquals(1190069,actor1.id());
    }

    @Test
    void getAllGenres(){
        List<Genre> genres =  TmdbService.getAllGenres();

        assertNotNull(genres);
        assertEquals("Action",genres.get(0).getName());
        assertEquals(19,genres.size());
    }
}