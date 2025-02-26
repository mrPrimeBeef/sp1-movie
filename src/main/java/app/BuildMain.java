package app;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import app.daos.DirectorDao;
import app.entities.Director;
import app.threads.CallableThread;
import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.entities.Movie;
import app.services.TmdbService;
import app.daos.GenreDao;
import app.entities.Genre;
import app.daos.ActorDao;
import app.daos.MovieDao;
import app.entities.Actor;

public class BuildMain {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        GenreDao genreDao = GenreDao.getInstance(emf);
        MovieDao movieDao = MovieDao.getInstance(emf);
        ActorDao actorDao = ActorDao.getInstance(emf);
        DirectorDao directorDao = DirectorDao.getInstance(emf);

        // Get all genres from TMDB and persists them in database
        List<Genre> genres = TmdbService.getAllGenres();
        genres.forEach(genreDao::create);

        // Create genreMap between tmdbId and Genre
        Map<Integer, Genre> genreMap = new HashMap<>();
        genres.forEach(g -> genreMap.put(g.getTmdbId(), g));

        // Get all movies from TMDB - we need genreMap to put genre entity inside movie entity
        List<Movie> movies = TmdbService.getDanishMoviesSince2020(genreMap);
        movies.forEach(movieDao::create);

        // Get all Directors and add them to DB
        addDirectors(movies, directorDao, movieDao);

        emf.close();
    }

    private static void addDirectors(List<Movie> movies, DirectorDao directorDao, MovieDao movieDao) {
        HashSet<Director> allDirectorsInAllMovies = new HashSet<>();

        ExecutorService executor = Executors.newCachedThreadPool();
        Map<Movie, Future<List<Director>>> futureMap = new HashMap<>();

        // Start async tasks for hver film
        for (Movie movie : movies) {
            Future<List<Director>> future = executor.submit(() ->
                    TmdbService.getDirectors(TmdbService.getCrewAndActorsDetails(movie.getTmdbId().toString()))
            );
            futureMap.put(movie, future);
        }

        // Hent resultaterne og tilføj instruktørerne til de rigtige film
        for (Map.Entry<Movie, Future<List<Director>>> entry : futureMap.entrySet()) {
            Movie movie = entry.getKey();
            Future<List<Director>> future = entry.getValue();
            try {
                List<Director> directorsInThisMovie = future.get();
                List<Director> managedDirectors = new ArrayList<>();
                for (Director director : directorsInThisMovie) {
                    Director managedDirector = directorDao.findById(Integer.parseInt(director.getTmdbId()));
                    if (managedDirector == null) {
                        managedDirector = directorDao.create(director);
                    }
                    managedDirectors.add(managedDirector);
                }

                movie.setDirectors(managedDirectors);
                movieDao.update(movie);

                allDirectorsInAllMovies.addAll(managedDirectors);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }
}