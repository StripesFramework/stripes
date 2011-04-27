package net.sourceforge.stripes.examples.bugzooky.biz;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Maintains an in memory list of bugs in the system.
 *
 * @author Tim Fennell
 */
public class BugManager {
    /** Sequence number to use for generating IDs. */
    private static int idSequence = 0;

    /** Storage for all known bugs. */
    private static Map<Integer,Bug> bugs = new TreeMap<Integer,Bug>();

    static {
        ComponentManager cm = new ComponentManager();
        PersonManager pm = new PersonManager();

        Bug bug = new Bug();
        bug.setShortDescription("First ever bug in the system.");
        bug.setLongDescription("This is a test bug, and is the first one ever made.");
        bug.setOpenDate( new Date() );
        bug.setStatus( Status.Resolved );
        bug.setPriority( Priority.High );
        bug.setComponent( cm.getComponent(0) );
        bug.setOwner( pm.getPerson(3) );
        saveOrUpdateInternal(bug);

        bug = new Bug();
        bug.setShortDescription("Another bug!  Oh no!.");
        bug.setLongDescription("How terrible - I found another bug.");
        bug.setOpenDate( new Date() );
        bug.setStatus( Status.Assigned );
        bug.setPriority( Priority.Blocker );
        bug.setComponent( cm.getComponent(2) );
        bug.setOwner( pm.getPerson(4) );
        saveOrUpdateInternal(bug);

        bug = new Bug();
        bug.setShortDescription("Three bugs?  This is just getting out of hand.");
        bug.setLongDescription("What kind of system has three bugs?  Egads.");
        bug.setOpenDate( new Date() );
        bug.setStatus( Status.New );
        bug.setPriority( Priority.High );
        bug.setComponent( cm.getComponent(0) );
        bug.setOwner( pm.getPerson(1) );
        saveOrUpdateInternal(bug);

        bug = new Bug();
        bug.setShortDescription("Oh good lord - I found a fourth bug.");
        bug.setLongDescription("That's it, you're all fired.  I need some better developers.");
        bug.setOpenDate( new Date() );
        bug.setStatus( Status.New );
        bug.setPriority( Priority.Critical );
        bug.setComponent( cm.getComponent(3) );
        bug.setOwner( pm.getPerson(0) );
        saveOrUpdateInternal(bug);

        bug = new Bug();
        bug.setShortDescription("Development team gone missing.");
        bug.setLongDescription("No, wait! I didn't mean it!  Please come back and fix the bugs!!");
        bug.setOpenDate( new Date() );
        bug.setStatus( Status.New );
        bug.setPriority( Priority.Blocker );
        bug.setComponent( cm.getComponent(2) );
        bug.setOwner( pm.getPerson(5) );
        saveOrUpdateInternal(bug);
    }

    /** Gets the bug with the corresponding ID, or null if it does not exist. */
    public Bug getBug(int id) {
        return bugs.get(id);
    }

    /** Returns a sorted list of all bugs in the system. */
    public List<Bug> getAllBugs() {
        return Collections.unmodifiableList( new ArrayList<Bug>(bugs.values()) );
    }

    /** Updates an existing bug, or saves a new bug if the bug is a new one. */
    public void saveOrUpdate(Bug bug) {
        saveOrUpdateInternal(bug);
    }

    /** Static helper so that it can be used both by the instance method, and in static init. */
    private static void saveOrUpdateInternal(Bug bug) {
        if (bug.getId() == null) {
            bug.setId(idSequence++);
        }

        bugs.put(bug.getId(), bug);
    }
}
