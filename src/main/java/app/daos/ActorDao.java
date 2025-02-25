package app.daos;

import jakarta.persistence.EntityManagerFactory;

import app.entities.Actor;

public class ActorDao extends AbstractDao<Actor, Long> {

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

}
