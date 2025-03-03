package app.daos;

import app.dtos.MovieDTO;
import app.entities.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TestData extends TestSetUp {

    public static void loadTestDataFromJson() {
        try {

            Path path = Paths.get("src/main/resources/testdata.json");
            String json = Files.readString(path);

            List<Movie> movies = convertJsonToMovieList(json);

            // Gem i databasen
            EntityManager em = emf.createEntityManager();;
            em.getTransaction().begin();

            for (Movie movie : movies) {
                // Gem skuespillere først
                for (JoinMovieActor join : movie.getJoins()) {
                    em.persist(join.getActor());
                }

                // Gem genrer
                for (Genre genre : movie.getGenres()) {
                    em.persist(genre);
                }

                // Gem instruktører
                for (Director director : movie.getDirectors()) {
                    em.persist(director);
                }

                // Gem film
                em.persist(movie);

                // Gem joins
                for (JoinMovieActor join : movie.getJoins()) {
                    em.persist(join);
                }
            }

            em.getTransaction().commit();
            em.close();

            System.out.println("Testdata indlæst fra JSON og brugt i test databasen");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Movie> convertJsonToMovieList(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MovieDTO[] dtos = objectMapper.readValue(json, MovieDTO[].class);
        List<Movie> movies = Arrays.stream(dtos)
                .map(TestData::convertDTOToMovie)
                .collect(Collectors.toList());

        return movies;
    }

    private static Movie convertDTOToMovie(MovieDTO dto) {
        // Opret genrer
        List<Genre> genres = dto.genres().stream()
                .map(genreDTO -> Genre.builder()
                        .tmdbId(genreDTO.tmdbId())
                        .name(genreDTO.name())
                        .build())
                .collect(Collectors.toList());

        // Opret instruktører
        List<Director> directors = dto.directors().stream()
                .map(directorDTO -> Director.builder()
                        .name(directorDTO.name())
                        .gender(directorDTO.gender())
                        .popularity(directorDTO.popularity())
                        .tmdbId(directorDTO.tmdbId())
                        .build())
                .collect(Collectors.toList());

        // Opret film
        Movie movie = Movie.builder()
                .tmdbId(dto.tmdbId())
                .title(dto.title())
                .originalTitle(dto.originalTitle())
                .overview(dto.overview())
                .adult(dto.adult())
                .originalLanguage(dto.originalLanguage())
                .popularity(dto.popularity())
                .releaseDate(dto.releaseDate())
                .build();

        // Sæt genrer og instruktører
        movie.setGenres(genres);
        movie.setDirectors(directors);

        // Opret og tilføj skuespillere via JoinMovieActor
        Set<JoinMovieActor> joins = dto.actors().stream()
                .map(actorDTO -> {
                    Actor actor = Actor.builder()
                            .name(actorDTO.name())
                            .gender(actorDTO.gender())
                            .popularity(actorDTO.popularity())
                            .tmdbId(actorDTO.tmdbId())
                            .build();

                    return JoinMovieActor.builder()
                            .movie(movie)
                            .actor(actor)
                            .character(actorDTO.character())
                            .build();
                })
                .collect(Collectors.toSet());

        movie.setJoins(joins);

        return movie;
    }
}