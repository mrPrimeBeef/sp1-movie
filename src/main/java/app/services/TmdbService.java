package app.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            for (int page = 1; page<2; page++) {

                String url = "https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&primary_release_date.gte=2020-01-01&with_origin_country=DK&page=" + page + "&api_key=" + ApiKey;
                String json = ApiReader.getDataFromUrl(url);

                MovieResponseDto response = objectMapper.readValue(json, MovieResponseDto.class);
                for (MovieResult r : response.results) {

                    // Use genreMap to go from tmdbId to Genre entity
                    List<Genre> genres = new ArrayList<>();
                    for (Integer tmdbId : r.genreIds) {
                        genres.add(genreMap.get(tmdbId));
                    }

                    movies.add(new Movie(null, r.tmdbId, r.title, r.originalTitle, r.overview, r.adult, r.originalLanguage, r.popularity, r.releaseDate, null, null, genres));
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

    private static MovieCastDto getCrewAndActorsDetails(String movieId) {

        MovieCastDto movieCastDto = null;

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

    public static List<Genre> getAllGenres() {

        ArrayList<Genre> genres = new ArrayList<>();

        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {

            GenresResponseDto response = objectMapper.readValue(json, GenresResponseDto.class);
            for (GenreDto g : response.genres) {
                genres.add(new Genre(null, g.tmdbId, g.name));
            }

            return genres;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Actor convertFromActorDtoToActor(ActorDto actorDto) {
        return Actor.builder()
                .name(actorDto.name)
                .gender(actorDto.gender)
                .popularity(actorDto.popularity)
                .build();
    }

    public static List<ActorDto> getActorDto(String movieID) {
        return getCrewAndActorsDetails(movieID).cast;
    }

    public static List<Actor> getActors(List<ActorDto> dto) {
        return dto.stream().map(TmdbService::convertFromActorDtoToActor).toList();
    }

    public static List<DirectorDto> getDirectorDto(String movieID) {
        List<DirectorDto> list = getCrewAndActorsDetails(movieID).crew;
        return list
                .stream()
                .filter(person -> "Director".equals(person.job()))
                .collect(Collectors.toList());
    }

    public static Director convertFromDirectorDtoToDirector(DirectorDto directorDto) {
        return Director.builder()
                .name(directorDto.name)
                .gender(directorDto.gender)
                .popularity(directorDto.popularity)
                .build();
    }

    public static List<Director> getDirectors(List<DirectorDto> dto) {
        return dto.stream().map(TmdbService::convertFromDirectorDtoToDirector).toList();
    }

    private record GenresResponseDto(List<GenreDto> genres) {
    }

    private record GenreDto(@JsonProperty("id")
                            Integer tmdbId,
                            String name) {
    }


    private record MovieCastDto(
            Integer id,
            List<ActorDto> cast,
            List<DirectorDto> crew) {

    }

    public record DirectorDto(
            String name,
            Gender gender,
            String job,
            double popularity) {
    }

    private record ActorDto(
            @JsonProperty("id")
            Integer tmdbId,
            String name,
            Gender gender,
            double popularity,
            String character) {
    }

    private record MovieResponseDto(MovieResult[] results) {
    }

    private record MovieResult(@JsonProperty("id")
                               Integer tmdbId,
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
}