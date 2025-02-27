package app.daos;

import app.config.HibernateConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

import static app.daos.TestData.loadTestDataFromJson;

public abstract class TestSetUp {
    protected static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    protected static final MovieDao movieDao = MovieDao.getInstance(emf);

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Slet data fra de afhængige tabeller først (relationstabeller)
            em.createQuery("DELETE FROM JoinMovieActor").executeUpdate(); // Relation table
            em.createQuery("DELETE FROM Movie").executeUpdate();          // Delete movie entries after relation data

            // Slet data fra de primære entitetstabeller
            em.createQuery("DELETE FROM Actor").executeUpdate();
            em.createQuery("DELETE FROM Director").executeUpdate();
            em.createQuery("DELETE FROM Genre").executeUpdate();

            // Nulstil sekvenser
            em.createNativeQuery("ALTER SEQUENCE actor_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE director_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE genre_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE movie_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE joinmovieactor_id_seq RESTART WITH 1").executeUpdate();

            em.getTransaction().commit();

            loadTestDataFromJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("EntityManagerFactory closed.");
        }
    }
}