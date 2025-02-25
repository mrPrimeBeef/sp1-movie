package app;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import app.daos.GenreeDao;
import app.entities.Genree;
import app.daos.ActorDao;
import app.daos.MovieDao;
import app.entities.Actor;
import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.entities.Movie;
import app.services.TmdbService;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        GenreeDao genreeDao = GenreeDao.getInstance(emf);
        MovieDao movieDao = MovieDao.getInstance(emf);
        ActorDao actorDao = ActorDao.getInstance(emf);

        List<Movie> movies = TmdbService.getDanishMoviesSince2020();

        movies.forEach(movieDao::create);

        HashSet<Actor> allActorsInAllMovies = new HashSet<>();

        for (Movie movie : movies) {

            List<Actor> actorsInThisMovie = TmdbService.getActors(TmdbService.getActorDto(movie.getTmdbId().toString()));

            for (Actor actor : actorsInThisMovie) {
                allActorsInAllMovies.add(actor);
            }

        }

        allActorsInAllMovies.forEach(System.out::println);


        TmdbService.GenresResponseDto list = TmdbService.getAllGenres();
        List<Genree> list2 = TmdbService.getGenres(list.genres());
        list2.forEach(genreeDao::create);

    }
}