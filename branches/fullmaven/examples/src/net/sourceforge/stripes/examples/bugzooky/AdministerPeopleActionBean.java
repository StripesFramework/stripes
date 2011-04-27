package net.sourceforge.stripes.examples.bugzooky;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontBind;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.examples.bugzooky.biz.Person;
import net.sourceforge.stripes.examples.bugzooky.biz.PersonManager;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

/**
 * Manages the administration of People, from the Administer Bugzooky page. Receives a List
 * of People, which may include a new person and persists the changes. Also receives an
 * Array of IDs for people that are to be deleted, and deletes them.
 *
 * @author Tim Fennell
 */
public class AdministerPeopleActionBean extends BugzookyActionBean {
    private int[] deleteIds;

    @ValidateNestedProperties ({
        @Validate(field="username", required=true, minlength=3, maxlength=15),
        @Validate(field="password", minlength=6, maxlength=20),
        @Validate(field="firstName", required=true, maxlength=25),
        @Validate(field="lastName", required=true,  maxlength=25),
        @Validate(field="email", converter=EmailTypeConverter.class)
    })
    private List<Person> people;

    public int[] getDeleteIds() { return deleteIds; }
    public void setDeleteIds(int[] deleteIds) { this.deleteIds = deleteIds; }

    /**
     * If no list of people is set and we're not handling the "save" event then populate the list of
     * people and return it.
     */
    public List<Person> getPeople() {
        if (people == null && !"Save".equals(getContext().getEventName())) {
            people = new PersonManager().getAllPeople();
        }

        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }

    @DefaultHandler
    @DontBind
    public Resolution view() {
        return new ForwardResolution("/bugzooky/AdministerBugzooky.jsp");
    }

    @HandlesEvent("Save")
    public Resolution saveChanges() {
        PersonManager pm = new PersonManager();

        // Save any changes to existing people (and create new ones)
        for (Person person : people) {
            pm.saveOrUpdate(person);
        }

        // Then, if the user checked anyone off to be deleted, delete them
        if (deleteIds != null) {
            for (int id : deleteIds) {
                pm.deletePerson(id);
            }
        }

        return new RedirectResolution(getClass());
    }
}
