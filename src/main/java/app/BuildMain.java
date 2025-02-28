package app;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(2);

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


        // Start concurrent runnable tasks
        List<Future> futures = new LinkedList<>();
        for (Movie movie : movies) {
            futures.add(executorService.submit(new RunnableTaskGetCreditsForMovie(movie)));
        }

        // Wait for tasks to finish
        for (Future f : futures) {
            try {
                f.get(); // blocking call
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        emf.close();
        executorService.shutdown();

    }


    private static class RunnableTaskGetCreditsForMovie implements Runnable {

        Movie movie;

        RunnableTaskGetCreditsForMovie(Movie movie) {
            this.movie = movie;
        }

        @Override
        public void run() {

            System.out.println(movie.getId());

            // Remember a person can be member twice in this movie
            // Loop though members of this movie, create them as a person if they are not already created
            for (MemberDto member : TmdbService.getMembersForMovie(movie.getId())) {

                System.out.println(member);

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