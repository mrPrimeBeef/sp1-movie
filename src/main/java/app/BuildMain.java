package app;

import java.util.Set;

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


        Set<GenreDto> genres = TmdbService.getGenres();
        genres.forEach(System.out::println);

        Set<MovieDto> movies = TmdbService.getDanishMoviesSince2020();
        movies.forEach(System.out::println);

        Set<MemberDto> persons = TmdbService.getMembersForMovieId(1029880);
//        persons.forEach(personDao::create);


        emf.close();


    }

}