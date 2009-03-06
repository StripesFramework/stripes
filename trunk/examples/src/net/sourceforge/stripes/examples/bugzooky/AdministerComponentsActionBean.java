package net.sourceforge.stripes.examples.bugzooky;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontBind;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.examples.bugzooky.biz.Component;
import net.sourceforge.stripes.examples.bugzooky.biz.ComponentManager;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

/**
 * Manages the administration of Components, from the Administer Bugzooky page. Receives a List
 * of Components, which may include a new component and persists the changes. Also receives an
 * Array of IDs for components that are to be deleted, and deletes those.
 *
 * @author Tim Fennell
 */
public class AdministerComponentsActionBean extends BugzookyActionBean {
    private int[] deleteIds;

    @ValidateNestedProperties ({
        @Validate(field="name", required=true, minlength=3, maxlength=25)
    })
    private List<Component> components;

    public int[] getDeleteIds() { return deleteIds; }
    public void setDeleteIds(int[] deleteIds) { this.deleteIds = deleteIds; }

    /**
     * If no list of components is set and we're not handling the "save" event then populate the
     * list of components and return it.
     */
    public List<Component> getComponents() {
        if (components == null && !"save".equals(getContext().getEventName())) {
            components = new ComponentManager().getAllComponents();
        }

        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    @DefaultHandler
    @DontBind
    public Resolution view() {
        return new ForwardResolution("/bugzooky/AdministerBugzooky.jsp");
    }

    public Resolution save() {
        ComponentManager cm = new ComponentManager();

        // Save any changes to existing components (and create new ones)
        for (Component component : components) {
            cm.saveOrUpdate(component);
        }

        // Then, if the user checked anyone off to be deleted, delete them
        if (deleteIds != null) {
            for (int id : deleteIds) {
                cm.deleteComponent(id);
            }
        }

        return new RedirectResolution(getClass());
    }
}
