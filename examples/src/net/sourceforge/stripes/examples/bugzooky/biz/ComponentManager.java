package net.sourceforge.stripes.examples.bugzooky.biz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Maintains a list of components in an in memory "database".
 *
 * @author Tim Fennell
 */
public class ComponentManager {
    /** Sequence number used to generated IDs. */
    private static int idSequence = 0;

    /** Storage for all known components. */
    private static Map<Integer,Component> components = new TreeMap<Integer,Component>();

    static {
        Component component = new Component("Component 0");
        saveOrUpdateInternal(component);

        component = new Component("Component 1");
        saveOrUpdateInternal(component);

        component = new Component("Component 2");
        saveOrUpdateInternal(component);

        component = new Component("Component 3");
        saveOrUpdateInternal(component);

        component = new Component("Component 4");
        saveOrUpdateInternal(component);
    }

    /** Gets the component with the specified ID, or null if no such component exists. */
    public Component getComponent(int id) {
        return components.get(id);
    }

    /** Returns a sorted list of all components in the system. */
    public List<Component> getAllComponents() {
        return Collections.unmodifiableList( new ArrayList<Component>(components.values()) );
    }

    /** Updates an existing component if the ID matches, or saves a new one otherwise. */
    public void saveOrUpdate(Component component) {
        saveOrUpdateInternal(component);
    }

    /** Deletes an existing Components.  May leave dangling references. */
    public void deleteComponent(int componentId) {
        components.remove(componentId);
    }

    private static void saveOrUpdateInternal(Component component) {
        if (component.getId() == null) {
            component.setId(idSequence++);
        }

        components.put(component.getId(), component);
    }
}
