package app.daos;

import jakarta.persistence.EntityManagerFactory;

import app.entities.Genre;
import app.dtos.GenreDto;

public class GenreDao extends AbstractDao<Genre, Integer> {

    private static GenreDao instance;

    private GenreDao(EntityManagerFactory emf) {
        super(Genre.class, emf);
    }

    public static GenreDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new GenreDao(emf);
        }
        return instance;
    }


    public Genre create(GenreDto g) {
        return create(new Genre(g.id(), g.name()));
    }


}