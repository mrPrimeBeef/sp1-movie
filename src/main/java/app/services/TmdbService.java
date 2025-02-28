package app.services;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.dtos.MovieDto;
import app.dtos.GenreDto;
import app.dtos.PersonDto;
import app.utils.ApiReader;
import app.utils.Utils;

public class TmdbService {

    private static final String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");

    public static Set<GenreDto> getGenres() {

        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            GenresResponseDto response = objectMapper.readValue(json, GenresResponseDto.class);
            return response.genres;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

    }


    public static Set<MovieDto> getDanishMoviesSince2020() {

        Set<MovieDto> movies = new HashSet<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        // TODO: HUsk at slette page<2
        for (int page = 1; ; page++) {

            String url = "https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&primary_release_date.gte=2020-01-01&with_origin_country=DK&page=" + page + "&api_key=" + ApiKey;
            String json = ApiReader.getDataFromUrl(url);

            MovieResponseDto response;

            try {
                response = objectMapper.readValue(json, MovieResponseDto.class);
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


    public static Set<PersonDto> getPersonsForMovieId(int movieId) {

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

        Set<PersonDto> persons = new HashSet<>();

        for (PersonDto p : response.cast) {
            persons.add(new PersonDto(p.id(), p.name(), p.gender(), p.popularity(), "Actor", p.character()));
        }

        persons.addAll(response.crew);

        return persons;

    }

    private record CreditsResponseDto(
            Set<PersonDto> cast,
            Set<PersonDto> crew) {
    }

    private record MovieResponseDto(Set<MovieDto> results) {
    }

    private record GenresResponseDto(Set<GenreDto> genres) {
    }

}