package app;

import app.config.HibernateConfig;
import app.daos.GenreDao;
import app.daos.MovieDao;
import app.daos.PersonDao;
import jakarta.persistence.EntityManagerFactory;

public class FuncMain {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("update");
        MovieDao movieDao = MovieDao.getInstance(emf);
        PersonDao personDao = PersonDao.getInstance(emf);

//        System.out.println("List of all movies pulled from the database:");
//        movieDao.readAll().forEach(System.out::println);
//
//        System.out.println("\nList of all persons pulled from the database:");
//        personDao.readAll().forEach(System.out::println);

        System.out.println("\nGenres in the movie 'Wild Men':");
        System.out.println(movieDao.FindAllGenreByMovieTitle("Wild Men"));

        System.out.println("All movies with genre comedy: " + movieDao.FindAllMoivesByGenre("Comedy"));

        System.out.println(movieDao.searchMovieByString("wild men"));

        System.out.println("average rating for all movies in DB: " + movieDao.averageRatingOfAllMoviesInDB());

        System.out.println("average rating for lowest rated movies in DB: " + movieDao.averageTop10LowestRating());

        System.out.println("average rating for higst rated movies in DB: " + movieDao.averageTop10HigestRating());


        emf.close();

    }
}