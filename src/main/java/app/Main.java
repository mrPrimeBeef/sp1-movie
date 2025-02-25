package app;

import java.util.HashSet;
import java.util.List;

import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.entities.Movie;
import app.services.TmdbService;
import app.daos.GenreDao;
import app.entities.Genre;
import app.daos.ActorDao;
import app.daos.MovieDao;
import app.entities.Actor;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        GenreDao genreDao = GenreDao.getInstance(emf);
        MovieDao movieDao = MovieDao.getInstance(emf);
        ActorDao actorDao = ActorDao.getInstance(emf);

        List<Genre> genres = TmdbService.getAllGenres();
        genres.forEach(System.out::println);
        genres.forEach(genreDao::create);
        genres.forEach(System.out::println);

//        List<Movie> movies = TmdbService.getDanishMoviesSince2020();
//
//        movies.forEach(movieDao::create);
//
//        HashSet<Actor> allActorsInAllMovies = new HashSet<>();
//
//        for (Movie movie : movies) {
//
//            List<Actor> actorsInThisMovie = TmdbService.getActors(TmdbService.getActorDto(movie.getTmdbId().toString()));
//
//            for (Actor actor : actorsInThisMovie) {
//                allActorsInAllMovies.add(actor);
//            }
//
//        }
//
//        allActorsInAllMovies.forEach(System.out::println);


        emf.close();

    }
}