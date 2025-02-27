package app;

import app.config.HibernateConfig;
import app.daos.GenreDao;
import app.daos.MovieDao;
import jakarta.persistence.EntityManagerFactory;

public class FuncMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        MovieDao movieDao = MovieDao.getInstance(emf);

        movieDao.findAll().forEach(System.out::println);

//        System.out.println( "Wild Men genres: " + movieDao.FindAllGenreByMovieTitle("Wild Men"));
//
//        System.out.println("All movies with genre comedy: " + movieDao.FindAllMoivesByGenre("Comedy"));

//        System.out.println(movieDao.searchMovieByString("wild men"));
//
//        System.out.println("average rating for all movies in DB: " + movieDao.averageRatingOfAllMoviesInDB());

//        System.out.println("average rating for lowest rated movies in DB: " + movieDao.averageTop10LowestRating());

//        System.out.println("average rating for higst rated movies in DB: " + movieDao.averageTop10HigestRating());


        emf.close();

    }
}