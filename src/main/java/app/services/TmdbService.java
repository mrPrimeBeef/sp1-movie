package app.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.entities.Movie;
import app.utils.ApiReader;
import app.utils.Utils;

public class TmdbService {

    public static List<Movie> getDanishMoviesSince2020() {

        ArrayList<Movie> movies = new ArrayList<>(1300);

        String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        try {

            for (int page = 1; ; page++) {

                String url = "https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&primary_release_date.gte=2020-01-01&sort_by=popularity.desc&with_origin_country=DK&page=" + page + "&api_key=" + ApiKey;
                String json = ApiReader.getDataFromUrl(url);

                ResponseMovieDto response = objectMapper.readValue(json, ResponseMovieDto.class);
                for (MovieResult r : response.results) {
                    movies.add(new Movie(null, r.tmdbId, r.title, r.originalTitle, r.overview, r.adult, r.originalLanguage, r.popularity, r.releaseDate, null, null, null));
                }

                if (response.results.length < 20) {
                    break;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return movies;

    }


    private record ResponseMovieDto(MovieResult[] results) {
    }

    private record MovieResult(@JsonProperty("id")
                               Integer tmdbId,
                               String title,
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
