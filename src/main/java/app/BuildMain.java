package app;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
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

        // Create genreMap between tmdbId and Genre
        Map<Integer, Genre> genreMap = new HashMap<>();
        genres.forEach(g -> genreMap.put(g.getTmdbId(), g));

        // Get all movies from TMDB - we need genreMap to put genre entity inside movie entity
        List<Movie> movies = TmdbService.getDanishMoviesSince2020(genreMap);
        movies.forEach(movieDao::create);

        // TODO: Gør sådan at tmdbId for actors ikke er null
        HashSet<Actor> allActorsInAllMovies = new HashSet<>();

        for (Movie movie : movies) {

            List<Actor> actorsInThisMovie = TmdbService.getActors(TmdbService.getActorDto(movie.getTmdbId().toString()));

            for (Actor actor : actorsInThisMovie) {
                allActorsInAllMovies.add(actor);
            }

        }
        allActorsInAllMovies.forEach(System.out::println);
        emf.close();
    }
}