package app.services;

import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManagerFactory;

import app.entities.*;
import app.utils.ApiReader;
import app.utils.Utils;
import app.config.HibernateConfig;
import app.daos.DirectorDao;
import app.daos.ActorDao;


public class TmdbService {

    private static final String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static final ActorDao actorDao = ActorDao.getInstance(emf);
    private static final DirectorDao directorDao = DirectorDao.getInstance(emf);

    public static void shutdown() {
        emf.close();
    }

    public static Set<Movie> getDanishMoviesSince2020(Map<Integer, Genre> genreMap) {

        // TODO: Necessary with initial capacity for hashset?
        HashSet<Movie> movies = new HashSet<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        try {
            // TODO: HUsk at slette page<2
            for (int page = 1; ; page++) {

                String url = "https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&primary_release_date.gte=2020-01-01&with_origin_country=DK&page=" + page + "&api_key=" + ApiKey;
                String json = ApiReader.getDataFromUrl(url);

                MovieResponseDto response = objectMapper.readValue(json, MovieResponseDto.class);
                for (MovieResult r : response.results) {

                    // Use genreMap to go from tmdbId to Genre entity
                    List<Genre> genres = new ArrayList<>();
                    for (Integer tmdbId : r.genreIds) {
                        genres.add(genreMap.get(tmdbId));
                    }

                    movies.add(new Movie(r.id, r.title, r.originalTitle, r.overview, r.adult, r.originalLanguage, r.popularity, r.releaseDate, null, null, genres));
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

    public static void addCreditsToMovie(Movie movie) {

        String url = "https://api.themoviedb.org/3/movie/" + movie.getId() + "/credits?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            CreditsResponseDto response = objectMapper.readValue(json, CreditsResponseDto.class);

            HashSet<Actor> actors = new HashSet<>();

            for (ActorDto a : response.cast) {

                Actor actor = actorDao.findById(a.id);
                if (actor == null) {
                    actor = actorDao.create(new Actor(a.id, a.name, a.gender, a.popularity, null));
                }

                actors.add(actor);
            }


            HashSet<Director> directors = new HashSet<>();

            for (DirectorDto d : response.crew) {
                if (d.job.equals("Director")) {

                    Director director = directorDao.findById(d.id);
                    if (director == null) {
                        director = directorDao.create(new Director(d.id, d.name, d.gender, d.popularity));
                    }

                    directors.add(director);
                }
            }

            movie.setDirectors(directors);
            // TODO: Fix this so it works
//            movie.setActors(actors);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static List<Genre> getAllGenres() {

        ArrayList<Genre> genres = new ArrayList<>();

        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);
        System.out.println(json);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {

            GenresResponseDto response = objectMapper.readValue(json, GenresResponseDto.class);
            for (GenreDto g : response.genres) {
                genres.add(new Genre(g.id, g.name));
            }

            return genres;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    private record CreditsResponseDto(
            List<ActorDto> cast,
            List<DirectorDto> crew) {
    }

    private record ActorDto(
            Integer id,
            String name,
            Gender gender,
            double popularity,
            String character) {
    }

    public record DirectorDto(
            Integer id,
            String name,
            Gender gender,
            String job,
            double popularity) {
    }


    private record MovieResponseDto(MovieResult[] results) {
    }

    private record MovieResult(Integer id,
                               String title,
                               @JsonProperty("original_title")
                               String originalTitle,
                               String overview,
                               Boolean adult,
                               @JsonProperty("original_language")
                               String originalLanguage,
                               Double popularity,
                               @JsonProperty("release_date")
                               LocalDate releaseDate,
                               @JsonProperty("genre_ids")
                               List<Integer> genreIds
    ) {
    }

    private record GenresResponseDto(List<GenreDto> genres) {
    }

    private record GenreDto(Integer id,
                            String name) {
    }

}