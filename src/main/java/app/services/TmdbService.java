package app.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.entities.*;
import app.utils.ApiReader;
import app.utils.Utils;


public class TmdbService {

    private static final String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");

    public static List<Movie> getDanishMoviesSince2020(Map<Integer, Genre> genreMap) {

        ArrayList<Movie> movies = new ArrayList<>(1300);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        try {
            // TODO: HUsk at slette page<2
            for (int page = 1; page < 2; page++) {

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

    private static CreditsResponseDto getCreditsForMovie(Integer tmdbMovieId) {

        CreditsResponseDto creditsResponseDto = null;

        String url = "https://api.themoviedb.org/3/movie/" + tmdbMovieId.toString() + "/credits?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            creditsResponseDto = objectMapper.readValue(json, CreditsResponseDto.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return creditsResponseDto;
    }

    public static List<Genre> getAllGenres() {

        ArrayList<Genre> genres = new ArrayList<>();

        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

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


    public static List<Actor> getActorsForMovie(int tmdbMovieId) {

        CreditsResponseDto creditsResponseDto = getCreditsForMovie(tmdbMovieId);

        List<Actor> actors = new LinkedList<>();
        for (ActorDto a : creditsResponseDto.cast) {
            actors.add(new Actor(a.id, a.name, a.gender, a.popularity, null));
        }

        return actors;
    }

    public static List<Director> getDirectorsForMovie(int tmdbMovieId) {

        CreditsResponseDto creditsResponseDto = getCreditsForMovie(tmdbMovieId);

        List<Director> directors = new LinkedList<>();
        for (DirectorDto d : creditsResponseDto.crew) {
            if (d.job.equals("Director")) {
                directors.add(new Director(d.tmdbId, d.name, d.gender, d.popularity, null));
            }

        }

        return directors;
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
            @JsonProperty("id")
            Integer tmdbId,
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