package app;

import java.util.*;

import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.entities.Movie;
import app.services.TmdbService;
import app.daos.GenreDao;
import app.entities.Genre;
import app.daos.MovieDao;


public class BuildMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        GenreDao genreDao = GenreDao.getInstance(emf);
        MovieDao movieDao = MovieDao.getInstance(emf);

        // Get all genres from TMDB and persists them in database
        List<Genre> genres = TmdbService.getAllGenres();
        genres.forEach(genreDao::create);

        // Create genreMap between id and Genre
        Map<Integer, Genre> genreMap = new HashMap<>();
        genres.forEach(g -> genreMap.put(g.getId(), g));

        // Get all movies from TMDB - we need genreMap to put genre entity inside movie entity
        Set<Movie> movies = TmdbService.getDanishMoviesSince2020(genreMap);
        movies.forEach(TmdbService::addCreditsToMovie);
        movies.forEach(System.out::println);
        movies.forEach(movieDao::create);


        emf.close();
        TmdbService.shutdown();

    }
}