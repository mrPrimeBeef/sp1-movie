package app.daos;

import app.entities.Genre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import app.entities.Movie;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class MovieDao extends AbstractDao<Movie, Integer> {

    private static MovieDao instance;

    private MovieDao(EntityManagerFactory emf) {
        super(Movie.class, emf);
    }

    public static MovieDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new MovieDao(emf);
        }
        return instance;
    }

    public List<Genre> FindAllGenreByMovieTitle(String title) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT g FROM Movie m JOIN m.genres g WHERE m.title = :title";
            TypedQuery query = em.createQuery(jpql, Genre.class);
            query.setParameter("title", title);
            return query.getResultList();
        }
    }

    public List<Movie> FindAllMoivesByGenre(String genre) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Movie m JOIN m.genres g WHERE g.name = :genre";
            TypedQuery query = em.createQuery(jpql, Movie.class);
            query.setParameter("genre", genre);
            return query.getResultList();
        }
    }

    public List<Movie> searchMovieByString(String title) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Movie m WHERE LOWER(m.title) = LOWER(:title)";
            TypedQuery query = em.createQuery(jpql, Movie.class);
            query.setParameter("title", title);
            return query.getResultList();
        }
    }

    public Double averageRatingOfAllMoviesInDB() {
        try (EntityManager em = emf.createEntityManager()) {
            String jqpl = "SELECT AVG(m.popularity) FROM Movie m";
            TypedQuery<Double> query = em.createQuery(jqpl, Double.class);
            return query.getSingleResult();
        }
    }

    public Double averageTop10LowestRating() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Movie m ORDER BY m.popularity ASC";
            TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
            query.setMaxResults(10);
            List<Movie> top10LowestMovies = query.getResultList();

            if (top10LowestMovies.isEmpty()) {
                return 0.0;
            }

            double sum = 0.0;
            for (Movie movie : top10LowestMovies) {
                sum += movie.getPopularity();
            }

            return sum / top10LowestMovies.size();
        }
    }

    public Double averageTop10HigestRating() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Movie m ORDER BY m.popularity DESC";
            TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
            query.setMaxResults(10);
            List<Movie> top10HigstMovies = query.getResultList();

            if (top10HigstMovies.isEmpty()) {
                return 0.0;
            }

            double sum = 0.0;
            for (Movie movie : top10HigstMovies) {
                sum += movie.getPopularity();
            }

            return sum / top10HigstMovies.size();
        }
    }
}