package app.services;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.dtos.MovieDto;
import app.dtos.GenreDto;
import app.dtos.CreditDto;
import app.utils.ApiReader;
import app.utils.Utils;

public class TmdbService {

    private static final String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");

    public static Set<GenreDto> getGenres() {

        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        GenresResponseDto response;

        try {
            response = objectMapper.readValue(json, GenresResponseDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        return response.genres;

    }


    public static Set<MovieDto> getDanishMoviesSince2020() {

        Set<MovieDto> movies = new HashSet<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        // TODO: Slet page<2
        for (int page = 1; ; page++) {

            String url = "https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&primary_release_date.gte=2020-01-01&with_origin_country=DK&page=" + page + "&api_key=" + ApiKey;
            String json = ApiReader.getDataFromUrl(url);

            MoviesResponseDto response;

            try {
                response = objectMapper.readValue(json, MoviesResponseDto.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }

            movies.addAll(response.results);

            if (response.results.size() < 20) {
                break;
            }
        }

        return movies;

    }


    public static Set<CreditDto> getCreditsForMovie(int movieId) {

        String url = "https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        CreditsResponseDto response;

        try {
            response = objectMapper.readValue(json, CreditsResponseDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        Set<CreditDto> credits = new HashSet<>();

        for (CreditDto c : response.cast) {
            credits.add(new CreditDto(c.personId(), c.name(), c.gender(), c.popularity(), "Actor", c.character()));
        }

        credits.addAll(response.crew);

        return credits;

    }

    private record CreditsResponseDto(
            Set<CreditDto> cast,
            Set<CreditDto> crew) {
    }

    private record MoviesResponseDto(Set<MovieDto> results) {
    }

    private record GenresResponseDto(Set<GenreDto> genres) {
    }

}