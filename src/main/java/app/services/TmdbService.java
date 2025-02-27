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
import lombok.AllArgsConstructor;
import lombok.Getter;


public class TmdbService {
//    private static List<ActorDto> actorDtos;
//    private static List<DirectorDto> directorDtos;

    private static final String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");

    public static List<Movie> getDanishMoviesSince2020(Map<Integer, Genre> genreMap) {

        ArrayList<Movie> movies = new ArrayList<>(1300);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        try {
            // TODO: HUsk at slette page<2 Der skal også laves threads, så der ikke fås 429 kode(too many api requests)
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

    public static MovieCastDTO getCrewAndActorsDetails(String movieId) {

        MovieCastDTO movieCastDTO = null;
        String url = "https://api.themoviedb.org/3/movie/" + movieId + "?append_to_response=credits&language=en-US&api_key=" + ApiKey;
        String json = ApiReader.getDataFromUrl(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        try {
            movieCastDTO = objectMapper.readValue(json, MovieCastDTO.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return movieCastDTO;
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

    public static Actor convertFromActorDtoToActor(CastDTO actorDTO) {
        return Actor.builder()
                .name(actorDTO.name)
                .gender(actorDTO.gender)
                .popularity(actorDTO.popularity)
                .tmdbId(actorDTO.id)
                .build();
    }

    public static List<ActorWithRole> getActors(MovieCastDTO dto) {
        return dto.credits.cast.stream()
                .map(actorDTO -> new ActorWithRole(
                        convertFromActorDtoToActor(actorDTO),
                        actorDTO.character
                ))
                .toList();
    }

    public static Director convertFromCastDtoToDirector(CrewDTO directorDto) {
        return Director.builder()
                .tmdbId(String.valueOf(directorDto.id))
                .name(directorDto.name)
                .gender(directorDto.gender)
                .build();
    }

    public static List<Director> getDirectors(MovieCastDTO dto) {
        List<Director> directors = dto.credits.crew.stream()
                .filter(person -> {
                    return "Director".equalsIgnoreCase(person.job);
                })
                .map(TmdbService::convertFromCastDtoToDirector)
                .toList();
        return directors;
    }

    private record GenresResponseDto(List<GenreDto> genres) {
    }

    private record GenreDto(@JsonProperty("id")
                            Integer tmdbId,
                            String name) {
    }


    public record MovieCastDTO(int id,
                               String imdbId,
                               CreditsDTO credits) {
    }

    public record CreditsDTO(List<CastDTO> cast,
                             List<CrewDTO> crew) {
    }

    public record CastDTO(boolean adult,
                          Gender gender,
                          int id,
                          String known_for_department,
                          String name,
                          String originalName,
                          double popularity,
                          String character,
                          String creditId,
                          String job) {
    }

    public record CrewDTO(boolean adult,
                          Gender gender,
                          int id,
                          String known_for_department,
                          String name,
                          String job) {
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