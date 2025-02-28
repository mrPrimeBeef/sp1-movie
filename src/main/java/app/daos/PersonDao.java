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


    public Person create(MemberDto m) {
        return create(new Person(m.id(), m.name(), m.gender(), m.popularity(), null));
    }

}