package app.daos;

import app.entities.Actor;
import app.entities.Director;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class DirectorDao extends AbstractDao<Director, Integer>{
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

    public List<Director> findDirectorsByMovie(Integer movieId) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT d FROM Director d JOIN FETCH d.movies m WHERE m.id = :movieId";
            TypedQuery<Director> query = em.createQuery(jpql, Director.class);
            query.setParameter("movieId", movieId);
            return query.getResultList();
        }
    }
}
