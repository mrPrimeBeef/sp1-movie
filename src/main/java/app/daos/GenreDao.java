package app.daos;

import app.dtos.GenreDto;
import jakarta.persistence.EntityManagerFactory;

import app.entities.Genre;

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


    public Genre update(GenreDto g) {
        return update(new Genre(g.id(), g.name()));
    }


}