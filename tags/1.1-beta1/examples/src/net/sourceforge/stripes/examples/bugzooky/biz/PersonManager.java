package net.sourceforge.stripes.examples.bugzooky.biz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Manager class that is used to access a "database" of people that is tracked in memory.
 */
public class PersonManager {
    /** Sequence counter for ID generation. */
    private static int idSequence = 0;

    /** Stores the list of people in the system. */
    private static Map<Integer,Person> people = new TreeMap<Integer,Person>();

    static {
        Person person = new Person("scooby", "Scooby", "Doo", "scooby@mystery.machine.tv");
        saveOrUpdateInternal(person);

        person = new Person("shaggy", "Shaggy", "Rogers", "shaggy@mystery.machine.tv");
        saveOrUpdateInternal(person);

        person = new Person("scrappy", "Scrappy", "Doo", "scrappy@mystery.machine.tv");
        saveOrUpdateInternal(person);

        person = new Person("daphne", "Daphne", "Blake", "daphne@mystery.machine.tv");
        saveOrUpdateInternal(person);

        person = new Person("velma", "Velma", "Dinkly", "velma@mystery.machine.tv");
        saveOrUpdateInternal(person);

        person = new Person("fred", "Fred", "Jones", "fred@mystery.machine.tv");
        saveOrUpdateInternal(person);
    }

    /** Returns the person with the specified ID, or null if no such person exists. */
    public Person getPerson(int id) {
        return people.get(id);
    }

    /** Gets a list of all the people in the system. */
    public List<Person> getAllPeople() {
        return Collections.unmodifiableList( new ArrayList<Person>(people.values()) );
    }

    /** Updates the person if the ID matches an existing person, otherwise saves a new person. */
    public void saveOrUpdate(Person person) {
        saveOrUpdateInternal(person);
    }

    /**
     * Deletes a person from the system...doesn't do anything fancy to clean up where the
     * person is used.
     */
    public void deletePerson(int id) {
        people.remove(id);
    }

    private static void saveOrUpdateInternal(Person person) {
        if (person.getId() == null) {
            person.setId(idSequence++);
        }

        people.put(person.getId(), person);
    }
}
