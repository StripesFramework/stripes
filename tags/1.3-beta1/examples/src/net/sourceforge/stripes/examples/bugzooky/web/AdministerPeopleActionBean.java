package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.examples.bugzooky.biz.Person;
import net.sourceforge.stripes.examples.bugzooky.biz.PersonManager;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.EmailTypeConverter;

import java.util.List;

/**
 * Manages the administration of People, from the Administer Bugzooky page. Receives a List
 * of People, which may include a new person and persists the changes. Also receives an
 * Array of IDs for people that are to be deleted, and deletes them.
 *
 * @author Tim Fennell
 */
@UrlBinding("/bugzooky/EditPeople.action")
public class AdministerPeopleActionBean extends BugzookyActionBean {
    private int[] deleteIds;
    private List<Person> people;

    public int[] getDeleteIds() { return deleteIds; }
    public void setDeleteIds(int[] deleteIds) { this.deleteIds = deleteIds; }

    @ValidateNestedProperties ({
        @Validate(field="username", required=true, minlength=3, maxlength=15),
        @Validate(field="password", minlength=6, maxlength=20),
        @Validate(field="firstName", required=true, maxlength=25),
        @Validate(field="lastName", required=true,  maxlength=25),
        @Validate(field="email", converter=EmailTypeConverter.class)
    })
    public List<Person> getPeople() { return people; }
    public void setPeople(List<Person> people) { this.people = people; }

    @HandlesEvent("Save") @DefaultHandler
    public Resolution saveChanges() {
        PersonManager pm = new PersonManager();

        // Apply any changes to existing people (and create new ones)
        for (Person person : people) {
            Person realPerson;
            if (person.getId() == null) {
                realPerson = new Person();
            }
            else {
                realPerson = pm.getPerson(person.getId());
            }

            realPerson.setEmail(person.getEmail());
            realPerson.setFirstName(person.getFirstName());
            realPerson.setLastName(person.getLastName());
            realPerson.setUsername(person.getUsername());

            if (person.getPassword() != null) {
                realPerson.setPassword(person.getPassword());
            }
            
            pm.saveOrUpdate(realPerson);
        }

        // Then, if the user checked anyone off to be deleted, delete them
        if (deleteIds != null) {
            for (int id : deleteIds) {
                pm.deletePerson(id);
            }
        }

        return new RedirectResolution("/bugzooky/AdministerBugzooky.jsp");
    }
}
