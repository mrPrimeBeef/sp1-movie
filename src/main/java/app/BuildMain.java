package app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import app.daos.DirectorDao;
import app.entities.*;
import app.threads.CallableThread;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.services.TmdbService;
import app.daos.GenreDao;
import app.daos.ActorDao;
import app.daos.MovieDao;
import jakarta.persistence.Tuple;

public class BuildMain {
    public static void main(String[] args) throws JsonProcessingException {

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

        // Get Directors and add to DB and movies
        addDirectors(movies, directorDao, movieDao);

        // Get Actors and add to DB and movies
        addActors(movies, actorDao,movieDao);

        // saveTestDataAsJson(movies);

        emf.close();
    }

    private static void addActors(List<Movie> movies, ActorDao actorDao, MovieDao movieDao) {
        HashSet<Actor> allActorsInAllMovies = new HashSet<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        Map<Movie, Future<List<ActorWithRole>>> futureMap = new HashMap<>();

        // Start async tasks for hver film
        for (Movie movie : movies) {
            Future<List<ActorWithRole>> future = executor.submit(() ->
                    TmdbService.getActors(TmdbService.getCrewAndActorsDetails(movie.getTmdbId().toString()))
            );
            futureMap.put(movie, future);
        }

        // Hent resultaterne og tilføj skuespillerne til de rigtige film
        for (Map.Entry<Movie, Future<List<ActorWithRole>>> entry : futureMap.entrySet()) {
            Movie movie = entry.getKey();
            Future<List<ActorWithRole>> future = entry.getValue();

            try {
                List<ActorWithRole> actorsInThisMovie = future.get();
                Set<JoinMovieActor> joins = new HashSet<>();

                for (ActorWithRole actorWithRole : actorsInThisMovie) {
                    Actor actor = actorWithRole.getActor();
                    String character = actorWithRole.getCharacter();

                    Actor managedActor = actorDao.findByTmdbId(actor.getTmdbId());
                    if (managedActor == null) {
                        managedActor = actorDao.create(actor);
                    }

                    JoinMovieActor join = JoinMovieActor.builder()
                            .movie(movie)
                            .actor(managedActor)
                            .character(character != null ? character : "Unknown")
                            .build();

                    joins.add(join);
                    allActorsInAllMovies.add(managedActor);
                }

                if (movie.getJoins() == null) {
                    movie.setJoins(new HashSet<>());
                }
                movie.getJoins().addAll(joins);

                movieDao.update(movie);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }

    private static HashSet<Director> addDirectors(List<Movie> movies, DirectorDao directorDao, MovieDao movieDao) {
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
        return allDirectorsInAllMovies;
    }

    public static void saveTestDataAsJson(List<Movie> movies) {
        try {
            String json = TmdbService.convertMovieListToJson(movies);

            // Gem JSON-strengen i en fil
            Path path = Paths.get("src/main/resources/testdata.json");
            Files.writeString(path, json);

            System.out.println("Testdata gemt som JSON");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}