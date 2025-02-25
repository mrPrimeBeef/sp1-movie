package app.daos;

import app.entities.Genree;
import jakarta.persistence.EntityManagerFactory;

public class GenreeDao extends AbstractDao<Genree, Integer>{
    private static GenreeDao instance;

    private GenreeDao(EntityManagerFactory emf) {
        super(Genree.class, emf);
    }

    public static GenreeDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new GenreeDao(emf);
        }
        return instance;
    }
}
