package app.daos;

import app.entities.Director;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import app.entities.Actor;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ActorDao extends AbstractDao<Actor, Integer> {

    private static ActorDao instance;

    private ActorDao(EntityManagerFactory emf) {
        super(Actor.class, emf);
    }

    public static ActorDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new ActorDao(emf);
        }
        return instance;
    }

    public Actor findByTmdbId(Integer tmdbId) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Actor> query = em.createQuery(
                    "SELECT a FROM Actor a WHERE a.tmdbId = :tmdbId", Actor.class);
            query.setParameter("tmdbId", tmdbId);
            return query.getResultStream().findFirst().orElse(null);
        }
    }

    public List<Actor> findActorsByMovie(Integer movieId) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT DISTINCT a FROM Actor a "
                    + "JOIN a.joins jma "
                    + "JOIN jma.movie m "
                    + "WHERE m.id = :movieId";
            TypedQuery<Actor> query = em.createQuery(jpql, Actor.class);
            query.setParameter("movieId", movieId);
            return query.getResultList();
        }
    }

}