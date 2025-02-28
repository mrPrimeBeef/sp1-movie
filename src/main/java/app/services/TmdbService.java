package app.services;

import java.time.LocalDate;
import java.util.*;

import app.daos.GenreDao;
import app.daos.MovieDao;
import app.dtos.MovieDto;
import app.dtos.GenreDto;

import app.dtos.PersonDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManagerFactory;

import app.entities.*;
import app.utils.ApiReader;
import app.utils.Utils;
import app.config.HibernateConfig;
import app.daos.PersonDao;


public class TmdbService {

    private static final String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("create");
    private static final PersonDao personDao = PersonDao.getInstance(emf);
    private static final GenreDao genreDao = GenreDao.getInstance(emf);
    private static final MovieDao movieDao = MovieDao.getInstance(emf);

    public static Set<GenreDto> getAllGenres() {

        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {

            GenresResponseDto response = objectMapper.readValue(json, GenresResponseDto.class);
            return response.genres;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static Set<MovieDto> getDanishMoviesSince2020() {

        // TODO: Necessary with initial capacity for hashset?
        HashSet<MovieDto> movies = new HashSet<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        try {
            // TODO: HUsk at slette page<2
            for (int page = 1; page < 5; page++) {

                String url = "https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&primary_release_date.gte=2020-01-01&with_origin_country=DK&page=" + page + "&api_key=" + ApiKey;
                String json = ApiReader.getDataFromUrl(url);

                MovieResponseDto response = objectMapper.readValue(json, MovieResponseDto.class);
                for (MovieDto m : response.results) {
                    movies.add(m);
                }

                if (response.results.length < 20) {
                    break;
                }
            }
            return movies;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static void addCreditsToMovie(Movie movie) {
//
//        String url = "https://api.themoviedb.org/3/movie/" + movie.getId() + "/credits?api_key=" + ApiKey;
//        String json = ApiReader.getDataFromUrl(url);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//        try {
//            CreditsResponseDto response = objectMapper.readValue(json, CreditsResponseDto.class);
//
//            HashSet<Credit> credits = new HashSet<>();
//
//            for (Member a : response.cast) {
//
//                Person person = personDao.findById(a.id);
//                if (person == null) {
//                    person = personDao.create(new Person(a.id, a.name, a.gender, a.popularity, null));
//                }
//
//                credits.add(new Credit(null, movie, person, "Actor", a.character));
//            }
//
//
//            for (Member d : response.crew) {
//
//                Person person = personDao.findById(d.id);
//                if (person == null) {
//                    person = personDao.create(new Person(d.id, d.name, d.gender, d.popularity, null));
//                }
//
//                credits.add(new Credit(null, movie, person, d.job, null));
//
//            }
//
//            movie.setCredits(credits);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }


    public static Set<PersonDto> getPersonsForMovieId(int movieId) {

        String url = "https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            Set<PersonDto> persons = new HashSet<>();

            CreditsResponseDto response = objectMapper.readValue(json, CreditsResponseDto.class);

            for (PersonDto p : response.cast) {
                persons.add(new PersonDto(p.id(), p.name(), p.gender(), p.popularity(), "Actor", p.character()));
            }

            persons.addAll(response.crew);

            return persons;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    private record CreditsResponseDto(
            Set<PersonDto> cast,
            Set<PersonDto> crew) {
    }


    private record MovieResponseDto(MovieDto[] results) {
    }


    private record GenresResponseDto(Set<GenreDto> genres) {
    }


    public static void main(String[] args) {


//        // Get all genres from TMDB and persists them in database
//        List<Genre> genres = TmdbService.getAllGenres();
//        genres.forEach(genreDao::create);
//
//        // Create genreMap between id and Genre
//        Map<Integer, Genre> genreMap = new HashMap<>();
//        genres.forEach(g -> genreMap.put(g.getId(), g));
//
//        // Get all movies from TMDB - we need genreMap to put genre entity inside movie entity
//        Set<Movie> movies = TmdbService.getDanishMoviesSince2020(genreMap);
//        movies.forEach(TmdbService::addCreditsToMovie);
//        movies.forEach(System.out::println);
//        movies.forEach(movieDao::create);
//
//
//        emf.close();

    }

}