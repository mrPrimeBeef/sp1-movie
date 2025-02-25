package app;

import java.util.Comparator;
import java.util.List;

import app.daos.GenreeDao;
import app.entities.Genree;
import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.entities.Movie;
import app.services.TmdbService;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        GenreeDao genreeDao = GenreeDao.getInstance(emf);

        List<Movie> movies = TmdbService.getDanishMoviesSince2020();
        movies.stream()
                .sorted(Comparator.comparing(Movie::getReleaseDate))
                .forEach(System.out::println);

        TmdbService.GenresResponseDto list = TmdbService.getAllGenres();
        List<Genree> list2 = TmdbService.getGenres(list.genres());
        list2.forEach(genreeDao::create);

    }
}