package net.sourceforge.stripes.examples.ex1;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jul 26, 2005 Time: 6:51:12 PM To change this
 * template use File | Settings | File Templates.
 */
public class Activity {
    private long id;
    private String name;
    private String description;

    public static Collection<Activity> getActivities() {
        Collection<Activity> activities = new ArrayList<Activity>();
        activities.add( new Activity(0, "Eating", "Eating") );
        activities.add( new Activity(1, "Sleeping", "Napping and such") );
        activities.add( new Activity(2, "Playing", "Chasing each other") );
        activities.add( new Activity(3, "Preening", "It's a cat thing") );
        activities.add( new Activity(4, "Playing Golf", "When the humans aren't looking") );

        return activities;
    }

    public Activity(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
