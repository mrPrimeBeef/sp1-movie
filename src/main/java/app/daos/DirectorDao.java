package app.daos;

import jakarta.persistence.EntityManagerFactory;

import app.entities.Director;

public class DirectorDao extends AbstractDao<Director, Integer> {

    private static DirectorDao instance;

    private DirectorDao(EntityManagerFactory emf) {
        super(Director.class, emf);
    }

    public static DirectorDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new DirectorDao(emf);
        }
        return instance;
    }

}
