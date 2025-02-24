package app.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public abstract class AbstractDao<T, I> {

    protected final EntityManagerFactory emf;
    protected final Class<T> entityClass;

    protected AbstractDao(Class<T> entityClass, EntityManagerFactory emf) {
        this.entityClass = entityClass;
        this.emf = emf;
    }

    public T create(T t) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(t);
            em.getTransaction().commit();
            return t;
        }
    }

    public T findById(I id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(entityClass, id);
        }
    }

    public List<T> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT t FROM " + entityClass.getSimpleName() + " t";
            return em.createQuery(jpql, entityClass).getResultList();
        }
    }

    public T update(T t) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(t);
            em.getTransaction().commit();
            return t;
        }
    }

    public void delete(I id) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "DELETE FROM " + entityClass.getSimpleName() + " t WHERE t.id = :id";
            em.getTransaction().begin();
            em.createQuery(jpql)
                    .setParameter("id", id)
                    .executeUpdate();
            em.getTransaction().commit();
        }
    }
}