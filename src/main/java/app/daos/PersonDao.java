package app.daos;

import app.dtos.MemberDto;
import jakarta.persistence.EntityManagerFactory;

import app.entities.Person;

public class PersonDao extends AbstractDao<Person, Integer> {

    private static PersonDao instance;

    private PersonDao(EntityManagerFactory emf) {
        super(Person.class, emf);
    }

    public static PersonDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new PersonDao(emf);
        }
        return instance;
    }


    public Person update(MemberDto p) {
        return update(new Person(p.id(), p.name(), p.gender(), p.popularity(), null));
    }

}