package app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

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

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("create");
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

        long startTime = System.nanoTime();

        System.out.println("Getting to Movies");
        // Get all movies from TMDB - we need genreMap to put genre entity inside movie entity
        List<Movie> movies = TmdbService.getDanishMoviesSince2020(genreMap);
        movies.forEach(movieDao::create);

        long slutTimeMovie = System.nanoTime();
        long varighedMovie = slutTimeMovie - startTime;

        double varighedSekunderMovie = varighedMovie / 1_000_000_000.0;
        System.out.println("point1 tog: " + varighedSekunderMovie + " sekunder");

        System.out.println("Getting to Directors");
        // Get Directors and add to DB and movies
        addDirectors(movies, directorDao, movieDao);

        long slutTimeDirector = System.nanoTime();
        long varighedDirector = slutTimeDirector - startTime;

        double varighedSekunderDirector = varighedDirector / 1_000_000_000.0;
        System.out.println("point2 tog: " + varighedSekunderDirector + " sekunder");

        System.out.println("Getting to actors");
        // Get Actors and add to DB and movies
        addActors(movies, actorDao,movieDao);

        long slutTimeActor = System.nanoTime();
        long varighedActor = slutTimeActor - startTime;

        double varighedSekunderActor = varighedActor / 1_000_000_000.0;
        System.out.println("point3 tog: " + varighedSekunderActor + " sekunder");

        // saveTestDataAsJson(movies);


        emf.close();
    }

    private static void addDirectors(List<Movie> movies, DirectorDao directorDao, MovieDao movieDao) {
        // Rate limiting configuration
        final int MAX_REQUESTS_PER_SECOND = 40;
        final long REQUEST_INTERVAL_MS = 1000 / MAX_REQUESTS_PER_SECOND; // 25ms between requests

        // Director cache to reduce database lookups
        Map<Integer, Director> directorCache = new HashMap<>();
        HashSet<Director> allDirectorsInAllMovies = new HashSet<>();

        // Create a scheduler for rate-limiting requests
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
        Map<Movie, Future<List<Director>>> futureMap = new HashMap<>();

        // Schedule tasks with rate limiting
        int requestCount = 0;
        for (Movie movie : movies) {
            final String tmdbId = movie.getTmdbId().toString();

            // Create a callable that gets director info
            Callable<List<Director>> directorTask = () ->
                    TmdbService.getDirectors(TmdbService.getCrewAndActorsDetails(tmdbId));

            // Schedule the task with appropriate delay
            Future<List<Director>> future = scheduler.schedule(
                    directorTask,
                    requestCount * REQUEST_INTERVAL_MS,
                    TimeUnit.MILLISECONDS);

            futureMap.put(movie, future);
            requestCount++;
        }

        // Process results in batches
        int batchSize = 20;
        List<Movie> moviesToUpdate = new ArrayList<>(batchSize);

        // Hent resultaterne og tilføj instruktørerne til de rigtige film
        for (Map.Entry<Movie, Future<List<Director>>> entry : futureMap.entrySet()) {
            Movie movie = entry.getKey();
            Future<List<Director>> future = entry.getValue();
            try {
                List<Director> directorsInThisMovie = future.get();
                List<Director> managedDirectors = new ArrayList<>();

                for (Director director : directorsInThisMovie) {
                    int directorTmdbId = Integer.parseInt(director.getTmdbId());

                    // Try to get director from cache first
                    Director managedDirector = directorCache.get(directorTmdbId);

                    if (managedDirector == null) {
                        // Not in cache, try database
                        managedDirector = directorDao.findById(directorTmdbId);

                        if (managedDirector == null) {
                            // Not in database either, create new
                            managedDirector = directorDao.create(director);
                        }

                        // Add to cache for future lookups
                        directorCache.put(directorTmdbId, managedDirector);
                    }

                    managedDirectors.add(managedDirector);
                }

                movie.setDirectors(managedDirectors);
                moviesToUpdate.add(movie);

                // Batch update to the database
                if (moviesToUpdate.size() >= batchSize) {
                    batchUpdateMovies(moviesToUpdate, movieDao);
                    moviesToUpdate.clear();
                }

                allDirectorsInAllMovies.addAll(managedDirectors);
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error processing directors for movie: " + movie.getTitle());
                e.printStackTrace();
            }
        }
        if (!moviesToUpdate.isEmpty()) {
            batchUpdateMovies(moviesToUpdate, movieDao);
        }
        scheduler.shutdown();
    }

    private static void addActors(List<Movie> movies, ActorDao actorDao, MovieDao movieDao) {
        // Rate limiting configuration
        final int MAX_REQUESTS_PER_SECOND = 40;
        final long REQUEST_INTERVAL_MS = 1000 / MAX_REQUESTS_PER_SECOND; // 25ms between requests

        HashSet<Actor> allActorsInAllMovies = new HashSet<>();

        // Use a fixed thread pool with limited size to control concurrency
        int threadPoolSize = Math.min(MAX_REQUESTS_PER_SECOND, Runtime.getRuntime().availableProcessors() * 2);
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // Use a semaphore to control the rate of submissions
        Semaphore rateLimiter = new Semaphore(MAX_REQUESTS_PER_SECOND);
        Map<Movie, Future<List<ActorWithRole>>> futureMap = new HashMap<>();

        // Start async tasks for each movie with rate limiting
        for (Movie movie : movies) {
            try {
                // Acquire permit before submitting task
                rateLimiter.acquire();

                Future<List<ActorWithRole>> future = executor.submit(() -> {
                    try {
                        List<ActorWithRole> result = TmdbService.getActors(
                                TmdbService.getCrewAndActorsDetails(movie.getTmdbId().toString()));
                        return result;
                    } finally {
                        // Schedule release of permit after minimum interval
                        executor.submit(() -> {
                            try {
                                Thread.sleep(REQUEST_INTERVAL_MS);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } finally {
                                rateLimiter.release();
                            }
                        });
                    }
                });

                futureMap.put(movie, future);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Rate limiting interrupted", e);
            }
        }

        // Process results in batches
        int batchSize = 20;
        List<Movie> moviesToUpdate = new ArrayList<>(batchSize);

        for (Map.Entry<Movie, Future<List<ActorWithRole>>> entry : futureMap.entrySet()) {
            Movie movie = entry.getKey();
            Future<List<ActorWithRole>> future = entry.getValue();

            try {
                List<ActorWithRole> actorsInThisMovie = future.get();
                Set<JoinMovieActor> joins = new HashSet<>();

                for (ActorWithRole actorWithRole : actorsInThisMovie) {
                    Actor actor = actorWithRole.getActor();
                    String character = actorWithRole.getCharacter();

                    // Consider caching actors to reduce database lookups
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
                moviesToUpdate.add(movie);

                // Batch update to the database
                if (moviesToUpdate.size() >= batchSize) {
                    batchUpdateMovies(moviesToUpdate, movieDao);
                    moviesToUpdate.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Error getting actors for movie: " + movie.getTitle(), e.getCause());
            }
        }
        if (!moviesToUpdate.isEmpty()) {
            batchUpdateMovies(moviesToUpdate, movieDao);
        }
        executor.shutdown();
    }


    private static void batchUpdateMovies(List<Movie> movies, MovieDao movieDao) {
        for (Movie movie : movies) {
            movieDao.update(movie);
        }
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