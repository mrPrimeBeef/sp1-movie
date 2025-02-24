package app;

import jakarta.persistence.EntityManagerFactory;

import app.config.HibernateConfig;
import app.services.TmdbService;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        TmdbService.getDanishMoviesSince2020();

    }
}