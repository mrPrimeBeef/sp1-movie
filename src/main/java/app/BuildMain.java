package app;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import app.entities.Credit;
import app.entities.Genre;
import app.entities.Movie;
import app.entities.Person;
import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.daos.GenreDao;
import app.daos.MovieDao;
import app.daos.PersonDao;
import app.dtos.GenreDto;
import app.dtos.MovieDto;
import app.dtos.MemberDto;
import app.services.TmdbService;

public class BuildMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("create");
        GenreDao genreDao = GenreDao.getInstance(emf);
        MovieDao movieDao = MovieDao.getInstance(emf);
        PersonDao personDao = PersonDao.getInstance(emf);

        // Get genreDtos from TmdbService, convert to Genre entities and create in database
        Set<Genre> genres = TmdbService
                .getGenres()
                .stream()
                .map(genreDao::create)
                .collect(Collectors.toUnmodifiableSet());


//        Set<Movie> movies = new HashSet<>();
//        for (MovieDto m : TmdbService.getDanishMoviesSince2020()) {
//
//            Set<Genre> genresForThisMovie = genres
//                    .stream()
//                    .filter(g -> m.genreIds().contains(g.getId()))
//                    .collect(Collectors.toUnmodifiableSet());
//
//            // Create movie in database
//            movies.add(movieDao.create(m, genresForThisMovie));
//        }
//
//        movies.forEach(System.out::println);
//
//
//        for (Movie movie : movies) {
//
//            for (MemberDto member : TmdbService.getMembersForMovieId(movie.getId())) {
//
//                if (personDao.findById(member.id()) == null)
//                    personDao.create(member);
//            }
//
//
//        }


        for (MovieDto m : TmdbService.getDanishMoviesSince2020()) {

            // Get all persons in this movie, and create them in database if they don't already exist
            Set<Person> personsInThisMovie = new HashSet<>();
            for (MemberDto member : TmdbService.getMembersForMovie(m.id())) {
                Person person = personDao.findById(member.id());
                if (person == null) {
                    person = personDao.create(member);
                }
                personsInThisMovie.add(person);
            }

            // Create set of credits for this movie
            Set<Credit> creditsForThisMovie = new HashSet<>();
//            creditsForThisMovie.add(new Credit(null, ));

        }


        emf.close();

    }

}