package app.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import app.entities.Director;
import app.entities.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.entities.Movie;
import app.utils.ApiReader;
import app.utils.Utils;

public class TmdbService {

    public static List<Movie> getDanishMoviesSince2020() {

        ArrayList<Movie> movies = new ArrayList<>(1200);

        String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");
        String url = "https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&language=en-US&page=1&release_date.gte=2020-01-01&sort_by=popularity.desc&api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        try {

            MovieResponseDto movieResponseDto = objectMapper.readValue(json, MovieResponseDto.class);
//            for (MovieResult r : movieResponseDto.movieResults) {
//                movies.add(new Movie(null, r.title, r.originalTitle, r.overview, r.adult, r.originalLanguage, r.popularity, r.releaseDate, null, null, null, null));
//            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return movies;

    }

    private static MovieCastDto getActorDetails(String movieId) {

        MovieCastDto movieCastDto = null;

        String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");
        String url = "https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        try {
            movieCastDto = objectMapper.readValue(json, MovieCastDto.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return movieCastDto;
    }

    public static List<ActorDto> getActorDto(String movieID) {
        return getActorDetails(movieID).cast;
    }

    public static List<DirectorDto> getDirectorDto(String movieID) {
        List<DirectorDto> list = getActorDetails(movieID).crew;
        return list
                .stream()
                .filter(person -> "Director".equals(person.job()))
                .collect(Collectors.toList());
    }

    private record MovieCastDto(
            Long id,
            List<ActorDto> cast,
            List<DirectorDto> crew) {

    }

    public record DirectorDto(
            Long id,
            String name,
            Gender gender,
            String job,
            double popularity) {
    }

    private record ActorDto(
            Long id,
            String name,
            Gender gender,
            double popularity,
            String character) {
    }

    private record MovieResponseDto(MovieResult[] movieResults) {
    }

    private record MovieResult(String title,
                               @JsonProperty("original_title")
                               String originalTitle,
                               Boolean adult,
                               @JsonProperty("original_language")
                               String originalLanguage,
                               Double popularity,
                               @JsonProperty("release_date")
                               LocalDate releaseDate,
                               @JsonProperty("genre_ids")
                               int[] genreIds,
                               String overview) {
    }


}