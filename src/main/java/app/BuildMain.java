package app;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.daos.GenreDao;
import app.daos.MovieDao;
import app.daos.PersonDao;
import app.dtos.MovieDto;
import app.dtos.CreditDto;
import app.entities.Genre;
import app.entities.Movie;
import app.entities.Person;
import app.services.TmdbService;

public class BuildMain {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("create");
    private static final GenreDao genreDao = GenreDao.getInstance(emf);
    private static final MovieDao movieDao = MovieDao.getInstance(emf);
    private static final PersonDao personDao = PersonDao.getInstance(emf);

    // TMDB says that approx. 40 request per second are allowed: https://www.themoviedb.org/talk/66eb8e189bd4250430746c22
    // To be on the safe side, this program limits to 30 requests per second
    private static final int MAX_REQUESTS_PER_SECOND = 30;
    private static final long DELAY_MILLISECONDS = 1000 / MAX_REQUESTS_PER_SECOND;

    public static void main(String[] args) {

        // Uses a fixed size thread pool. CachedThreadPool was tried, but was too fast for the database
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Get genreDtos from TmdbService, convert to Genre entities and create in database
        Set<Genre> genres = TmdbService
                .getGenres()
                .stream()
                .map(genreDao::create)
                .collect(Collectors.toUnmodifiableSet());


        Set<Movie> movies = new HashSet<>();
        for (MovieDto m : TmdbService.getDanishMoviesSince2020()) {

            Set<Genre> genresForThisMovie = genres.stream()
                    .filter(g -> m.genreIds().contains(g.getId()))
                    .collect(Collectors.toUnmodifiableSet());

            Movie movie = new Movie(m.id(), m.title(), m.originalTitle(), m.adult(), m.originalLanguage(), m.popularity(), m.releaseDate(), genresForThisMovie, null, m.overview());
            movieDao.create(movie);
            movies.add(movie);
        }


        // Start concurrent runnable tasks
        long startTime = System.currentTimeMillis();
        List<Future> futures = new LinkedList<>();
        for (Movie movie : movies) {

            Runnable task = new TaskGetCreditsForMovie(movie);
            futures.add(executor.submit(task));

            try {
                Thread.sleep(DELAY_MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        // Wait for tasks to finish
        for (Future future : futures) {
            try {
                future.get(); // blocking call
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Time it took: " + (System.currentTimeMillis() - startTime));

        emf.close();
        executor.shutdown();

    }


    private static class TaskGetCreditsForMovie implements Runnable {

        private Movie movie;

        TaskGetCreditsForMovie(Movie movie) {
            this.movie = movie;
        }

        @Override
        public void run() {

            // Remember a person can be member twice in this movie
            // Loop though members of this movie, create them as a person if they are not already created
            for (CreditDto c : TmdbService.getCreditsForMovie(movie.getId())) {

                // Get or create person in database
                Person person = personDao.update(new Person(c.personId(), c.name(), c.gender(), c.popularity(), null));

                // Add credit to movie entity in memory
                movie.addCredit(person, c.job(), c.character());

            }

            movieDao.update(movie);

            System.out.println(movie);

        }

    }


}