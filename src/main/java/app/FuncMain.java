package app;

import app.config.HibernateConfig;
import app.daos.GenreDao;
import app.daos.MovieDao;
import app.daos.PersonDao;
import app.entities.Movie;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class FuncMain {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("update");
        GenreDao genreDao = GenreDao.getInstance(emf);
        MovieDao movieDao = MovieDao.getInstance(emf);
        PersonDao personDao = PersonDao.getInstance(emf);

        System.out.println("List of all movies pulled from the database:");
        List<Movie> movies = movieDao.readAll();
        movies.forEach(System.out::println);
        System.out.println("Number of movies in database: " + movies.size());

        System.out.println("\nList of all persons in the movie 'The Marco Effect':");
        personDao.readPersonsByMovieId(659940).forEach(System.out::println);

        System.out.println("\nList of all actors in the movie 'The Marco Effect':");
        personDao.readPersonsByMovieIdAndJob(659940, "Actor").forEach(System.out::println);

        System.out.println("\nList of all directors in the movie 'The Marco Effect':");
        personDao.readPersonsByMovieIdAndJob(659940, "Director").forEach(System.out::println);

        System.out.println("\nList of all genres: ");
        genreDao.readAll().forEach(System.out::println);

        System.out.println("\nGenres in the movie 'The Marco Effect': ");
        movieDao.readGenresByMovieTitle("The Marco Effect").forEach(System.out::println);

        System.out.println("\nAll movies with genre 'Thriller': ");
        movieDao.readMoviesByGenre("Thriller").forEach(System.out::println);

        System.out.println("\nAll movies with keyword 'lov' somewhere in title or original title: ");
        movieDao.searchMoviesByKeywordInTitleOrOrignalTitle("lov").forEach(System.out::println);

        System.out.println("\nAverage rating of all movies: " + movieDao.getAverageRatingOfAllMovies());

        System.out.println("\nTop 10 highest rated movies, with at least 100 votes:");
        movieDao.getHighestRatedMovies(10).forEach(System.out::println);

        System.out.println("\nTop 10 lowest rated movies, with at least 100 votes:");
        movieDao.getLowestRatedMovies(10).forEach(System.out::println);

        System.out.println("\nTop 10 most popular movies:");
        movieDao.getMostPopularMovies(10).forEach(System.out::println);


        emf.close();

    }
}