package app;

import java.util.Set;

import app.dtos.GenreDto;
import app.dtos.MovieDto;
import app.dtos.PersonDto;
import app.services.TmdbService;

public class BuildMain {

    public static void main(String[] args) {

        Set<GenreDto> genres = TmdbService.getAllGenres();
        genres.forEach(System.out::println);

        Set<MovieDto> movies = TmdbService.getDanishMoviesSince2020();
        movies.forEach(System.out::println);

// 1029880
        Set<PersonDto> persons = TmdbService.getPersonsForMovieId(1029880);
        persons.forEach(System.out::println);

    }

}