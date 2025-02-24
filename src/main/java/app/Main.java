package app;

import app.config.HibernateConfig;
import app.services.TmdbService;
import app.utils.Utils;
import jakarta.persistence.EntityManagerFactory;

public class Main {
    public static void main(String[] args) {

        TmdbService.getDanishMoviesSince2020();

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    }
}