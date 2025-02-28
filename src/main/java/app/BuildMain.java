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
import app.dtos.MemberDto;
import app.entities.Genre;
import app.entities.Movie;
import app.entities.Person;
import app.services.TmdbService;

public class BuildMain {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("create");
    private static final GenreDao genreDao = GenreDao.getInstance(emf);
    private static final MovieDao movieDao = MovieDao.getInstance(emf);
    private static final PersonDao personDao = PersonDao.getInstance(emf);

    private static final int MAX_TASKS_PER_SECOND = 30; // Documentation says around 40 per second
    private static final long DELAY_MILLISECONDS = 1000 / MAX_TASKS_PER_SECOND;

    public static void main(String[] args) {

//        ExecutorService executor = Executors.newCachedThreadPool();
        ExecutorService executor = Executors.newFixedThreadPool(3);

//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);


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
            movies.add(movieDao.create(movie));
        }

        long timeA = System.currentTimeMillis();

        // Start concurrent runnable tasks
        List<Future> futures = new LinkedList<>();
        for (Movie movie : movies) {

            long startTime = System.currentTimeMillis();

            Runnable task = new TaskGetCreditsForMovie(movie);
            futures.add(executor.submit(task));

            long sleepTime = Math.max(DELAY_MILLISECONDS - (System.currentTimeMillis() - startTime), 0);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        // Wait for tasks to finish
        for (Future f : futures) {
            try {
                f.get(); // blocking call
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        long timeB = System.currentTimeMillis();
        System.out.println(timeB - timeA);

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
            for (MemberDto member : TmdbService.getMembersForMovie(movie.getId())) {

                // Get or create person in database
                Person person = personDao.update(new Person(member.id(), member.name(), member.gender(), member.popularity(), null));

                // Add credit to movie entity in memory
                movie.addCredit(person, member.job(), member.character());

            }

            movieDao.update(movie);

            System.out.println(movie);

        }

    }


}