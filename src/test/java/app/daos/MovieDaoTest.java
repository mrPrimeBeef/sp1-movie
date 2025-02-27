package app.daos;

import app.entities.Genre;
import app.entities.Movie;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovieDaoTest extends TestSetUp {

    @Test
    void findMovieById() {
        Movie movie = movieDao.findById(1);

        Movie movie1 = movieDao.findById(0);

        assertEquals("River of Blood", movie.getTitle());
        assertEquals("1222064", movie.getTmdbId().toString());

        assertNull(movie1);

        assertThrows(IllegalArgumentException.class,() -> movieDao.findById(null));
    }

    @Test
    void findAllGenreByMovieTitle() {
        List<Genre> genres = movieDao.FindAllGenreByMovieTitle("River of Blood");

        List<Genre> genres1 = movieDao.FindAllGenreByMovieTitle("River of vlood");

        assertEquals(2,genres.size());
        assertEquals("Horror" ,genres.get(0).getName());
        assertTrue(genres1.isEmpty());
    }

    @Test
    void findAllMoivesByGenre() {
        List<Movie> moviesList =  movieDao.FindAllMoivesByGenre("Horror");
        List<Movie> moviesList1 =  movieDao.FindAllMoivesByGenre("Horrror");

        assertEquals(3,moviesList.size());
        assertEquals(moviesList.get(0).getTitle(),"River of Blood");
        assertTrue(moviesList1.isEmpty());
    }

    @Test
    void searchMovieByString() {
    }

    @Test
    void averageRatingOfAllMoviesInDB() {
    }

    @Test
    void averageTop10LowestRating() {
    }

    @Test
    void averageTop10HigestRating() {
    }
}