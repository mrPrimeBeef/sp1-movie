package app;

import app.config.HibernateConfig;
import app.daos.ActorDao;
import app.daos.DirectorDao;
import app.daos.GenreDao;
import app.daos.MovieDao;
import jakarta.persistence.EntityManagerFactory;

public class FuncMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("update");
        MovieDao movieDao = MovieDao.getInstance(emf);
        DirectorDao directorDao = DirectorDao.getInstance(emf);
        ActorDao actorDao = ActorDao.getInstance(emf);

//        movieDao.findAll().forEach(System.out::println);

//        System.out.println( "Wild Men genres: " + movieDao.FindAllGenreByMovieTitle("Wild Men"));
//
//        System.out.println("All movies with genre comedy: " + movieDao.FindAllMoivesByGenre("Comedy"));

        System.out.println(movieDao.searchMovieByString("River of Blood"));

//        System.out.println("average rating for all movies in DB: " + movieDao.averageRatingOfAllMoviesInDB());

//        System.out.println("average rating for lowest rated movies in DB: " + movieDao.averageTop10LowestRating());

//        System.out.println("average rating for higst rated movies in DB: " + movieDao.averageTop10HigestRating());

//        System.out.println(directorDao.findDirectorsByMovie(2));

        System.out.println(actorDao.findActorsByMovie(1));
    }
}