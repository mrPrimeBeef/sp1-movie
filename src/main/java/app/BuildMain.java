package app;

import java.util.Set;
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


        for (MovieDto m : TmdbService.getDanishMoviesSince2020()) {

            Set<Genre> genresForThisMovie = genres.stream()
                    .filter(g -> m.genreIds().contains(g.getId()))
                    .collect(Collectors.toUnmodifiableSet());

            Movie movie = new Movie(m.id(), m.title(), m.originalTitle(), m.adult(), m.originalLanguage(), m.popularity(), m.releaseDate(), genresForThisMovie, null, m.overview());

            // Remember a person can be member twice in this movie
            // Loop though members of this movie, create them as a person if they are not already created
            for (MemberDto member : TmdbService.getMembersForMovie(m.id())) {

                Person person = personDao.findById(member.id());
                if (person == null) {
                    person = personDao.create(member);
                }

                movie.addCredit(person, member.job(), member.character());


            }

            movieDao.create(movie);

        }


        emf.close();

    }

}