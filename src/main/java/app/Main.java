package app;

import app.entities.Movie;
import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.services.TmdbService;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

//        List<Movie> movies = TmdbService.getDanishMoviesSince2020();
//        movies.forEach(System.out::println);


        String LordOfTheRingID = "120";
        TmdbService.getActorDto("LordOfTheRingID");
        System.out.println(TmdbService.getDirectorDto("LordOfTheRingID"));


    }
}