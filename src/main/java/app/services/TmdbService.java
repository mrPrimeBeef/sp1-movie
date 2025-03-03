package app.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import app.dtos.DirectorDTO;
import app.dtos.GenreDTO;
import app.dtos.MovieActorDTO;
import app.dtos.MovieDTO;
import app.exceptions.ApiException;
import app.threads.CallableThread;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.entities.*;
import app.utils.ApiReader;
import app.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;


public class TmdbService {

    private static final String ApiKey = Utils.getPropertyValue("API_KEY", "config.properties");

    public static List<Movie> getDanishMoviesSince2020(Map<Integer, Genre> genreMap) {
        final int MAX_REQUESTS_PER_SECOND = 40;
        final long REQUEST_INTERVAL_MS = 1000 / MAX_REQUESTS_PER_SECOND; // 25ms between requests

        ArrayList<Movie> movies = new ArrayList<>(1300);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
            for (int page = 1;; page++) {
                String url = "https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&primary_release_date.gte=2020-01-01&with_origin_country=DK&page=" + page + "&api_key=" + ApiKey;
                Future<String> json = scheduler.schedule(new CallableThread(url), REQUEST_INTERVAL_MS, TimeUnit.MILLISECONDS);

                MovieResponseDto response = objectMapper.readValue(json.get(), MovieResponseDto.class);
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
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            return movies;

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("There is an error in getting the API in getDanishMoviesSince2020()");
        }
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
            throw new ApiException("There is an error in getting the API in getCrewAndActorsDetails()");
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
            throw new ApiException("There is an error in getting the API in getCrewAndActorsDetails()");
        }
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

    public static String convertMovieListToJson(List<Movie> movies) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<MovieDTO> dtos = movies.stream()
                .map(TmdbService::convertMovieToDTO)
                .collect(Collectors.toList());
        String json = objectMapper.writeValueAsString(dtos);

        return json;
    }

    private static MovieDTO convertMovieToDTO(Movie movie) {
        List<DirectorDTO> directorDTOs = movie.getDirectors().stream()
                .map(director -> new DirectorDTO(
                        director.getName(),
                        director.getGender(),
                        director.getPopularity(),
                        director.getTmdbId()))
                .collect(Collectors.toList());

        List<GenreDTO> genreDTOs = movie.getGenres().stream()
                .map(genre -> new GenreDTO(
                        genre.getTmdbId(),
                        genre.getName()))
                .collect(Collectors.toList());

        List<MovieActorDTO> actorDTOs = movie.getJoins().stream()
                .map(join -> new MovieActorDTO(
                        join.getActor().getName(),
                        join.getActor().getGender(),
                        join.getActor().getPopularity(),
                        join.getActor().getTmdbId(),
                        join.getCharacter()))
                .collect(Collectors.toList());

        return new MovieDTO(
                movie.getId(),
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getOriginalTitle(),
                movie.getOverview(),
                movie.isAdult(),
                movie.getOriginalLanguage(),
                movie.getPopularity(),
                movie.getReleaseDate(),
                directorDTOs,
                genreDTOs,
                actorDTOs
        );
    }
}