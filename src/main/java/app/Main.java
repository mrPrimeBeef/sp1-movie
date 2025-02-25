package app;

import java.util.Comparator;
import java.util.List;

import app.daos.MovieDao;
import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.entities.Movie;
import app.services.TmdbService;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        MovieDao movieDao = MovieDao.getInstance(emf);

        List<Movie> movies = TmdbService.getDanishMoviesSince2020();

        movies.forEach(movieDao::create);

//        movies.stream()
//                .sorted(Comparator.comparing(Movie::getReleaseDate))
//                .forEach(System.out::println);




    }
}