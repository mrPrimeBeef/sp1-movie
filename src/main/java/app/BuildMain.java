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
import app.dtos.CreditDto;
import app.dtos.GenreDto;
import app.dtos.MovieDto;
import app.entities.Genre;
import app.entities.Movie;
import app.entities.Person;
import app.services.TmdbService;

public class BuildMain {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("create");
    private static final GenreDao genreDao = GenreDao.getInstance(emf);
    private static final MovieDao movieDao = MovieDao.getInstance(emf);
    private static final PersonDao personDao = PersonDao.getInstance(emf);

    public static void main(String[] args) {

        // Uses a fixed size thread pool. CachedThreadPool was tried, but was too fast for the database access
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // TMDB says that approx. 40 requests per second are allowed: https://www.themoviedb.org/talk/66eb8e189bd4250430746c22
        // To be on the safe side, this program limits to 30 requests per second
        int MAX_REQUESTS_PER_SECOND = 30;
        long DELAY_MILLISECONDS = 1000 / MAX_REQUESTS_PER_SECOND;

        long startTime = System.currentTimeMillis();

        // Get all genres from TMDB and persist them in database
        Set<Genre> genres = new HashSet<>();
        for (GenreDto g : TmdbService.getGenres()) {
            Genre genre = genreDao.create(new Genre(g.id(), g.name()));
            genres.add(genre);
            System.out.println("Got and persisted genre: " + genre);
        }

        // Get all danish movies since 2020 from TMDB and persist them in database
        Set<Movie> movies = new HashSet<>();
        for (MovieDto m : TmdbService.getDanishMoviesSince2020(DELAY_MILLISECONDS)) {

            Set<Genre> genresForThisMovie = genres.stream()
                    .filter(g -> m.genreIds().contains(g.getId()))
                    .collect(Collectors.toUnmodifiableSet());

            Movie movie = new Movie(m.id(), m.title(), m.originalTitle(), m.adult(), m.originalLanguage(), m.popularity(), m.voteAverage(), m.voteCount(), m.releaseDate(), genresForThisMovie, m.overview());
            movie = movieDao.create(movie);
            movies.add(movie);
            System.out.println("Got and persisted movie: " + movie);
        }


        // Loop through movies and get their credits from TMDB and persists them in database
        // This is done in parallel using threads
        List<Future> futures = new LinkedList<>();
        for (Movie movie : movies) {

            Runnable task = new GetAndPersistCreditsForMovie(movie);
            Future future = executor.submit(task);
            futures.add(future);

            try {
                Thread.sleep(DELAY_MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        for (Future future : futures) {
            try {
                future.get(); // blocking call, waits for task to finish
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Milliseconds it took: " + (System.currentTimeMillis() - startTime));

        emf.close();
        executor.shutdown();

    }


    private static class GetAndPersistCreditsForMovie implements Runnable {

        private Movie movie;

        GetAndPersistCreditsForMovie(Movie movie) {
            this.movie = movie;
        }

        @Override
        public void run() {

            for (CreditDto c : TmdbService.getCreditsForMovie(movie.getId())) {

                // This creates person in database if it does not already exist
                Person person = personDao.update(new Person(c.personId(), c.name(), c.gender(), c.popularity(), null));

                movie.addCredit(person, c.job(), c.character());

            }

            movieDao.update(movie);
            System.out.println("Got and persisted credits for movie: " + movie);

        }

    }

}