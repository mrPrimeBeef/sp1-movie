package app;

import java.util.*;

import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.entities.Director;
import app.entities.Movie;
import app.services.TmdbService;
import app.daos.GenreDao;
import app.entities.Genre;
import app.daos.ActorDao;
import app.daos.MovieDao;
import app.entities.Actor;

public class BuildMain {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        GenreDao genreDao = GenreDao.getInstance(emf);
        MovieDao movieDao = MovieDao.getInstance(emf);
        ActorDao actorDao = ActorDao.getInstance(emf);

        // Get all genres from TMDB and persists them in database
        List<Genre> genres = TmdbService.getAllGenres();
        genres.forEach(genreDao::create);

        // Create genreMap between id and Genre
        Map<Integer, Genre> genreMap = new HashMap<>();
        genres.forEach(g -> genreMap.put(g.getId(), g));

        // Get all movies from TMDB - we need genreMap to put genre entity inside movie entity
        Set<Movie> movies = TmdbService.getDanishMoviesSince2020(genreMap);
//        movies.forEach(System.out::println);
        movies.forEach(TmdbService::addCreditsToMovie);

//        for(Movie m:movies){
//            TmdbService.addCreditsToMovie(m);
//        }

        movies.forEach(System.out::println);
        movies.forEach(movieDao::create);

        // Add directors to all movies

//        // TODO: Gør sådan at tmdbId for actors ikke er null
//        HashSet<Actor> allActorsInAllMovies = new HashSet<>();
//        HashSet<Director> allDirectorsInAllMovies = new HashSet<>();
//
//        for (Movie movie : movies) {
//
//            List<Actor> actorsInThisMovie = TmdbService.getActorsForMovie(movie.getId());
//            for (Actor actor : actorsInThisMovie) {
//                allActorsInAllMovies.add(actor);
//            }
//
//            List<Director> directorsInThisMovie = TmdbService.getDirectorsForMovie(movie.getId());
//            for (Director director : directorsInThisMovie) {
//                allDirectorsInAllMovies.add(director);
//            }
//
//        }
//
//        allActorsInAllMovies.forEach(System.out::println);
//        allDirectorsInAllMovies.forEach(System.out::println);


        emf.close();
        TmdbService.shutdown();

    }
}