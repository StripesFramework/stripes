package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.examples.bugzooky.biz.Component;
import net.sourceforge.stripes.examples.bugzooky.biz.ComponentManager;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import java.util.List;

/**
 * Manages the administration of Components, from the Administer Bugzooky page. Receives a List
 * of Components, which may include a new component and persists the changes. Also receives an
 * Array of IDs for components that are to be deleted, and deletes those.
 *
 * @author Tim Fennell
 */
@UrlBinding("/bugzooky/EditComponents.action")
public class AdministerComponentsActionBean extends BugzookyActionBean {
    private int[] deleteIds;
    private List<Component> components;

    public int[] getDeleteIds() { return deleteIds; }
    public void setDeleteIds(int[] deleteIds) { this.deleteIds = deleteIds; }

    @ValidateNestedProperties ({
        @Validate(field="name", required=true, minlength=3, maxlength=25)        
    })
    public List<Component> getComponents() { return components; }
    public void setComponents(List<Component> components) { this.components = components; }

    @HandlesEvent("Save") @DefaultHandler
    public Resolution saveChanges() {
        ComponentManager cm = new ComponentManager();

        // Apply any changes to existing people (and create new ones)
        for (Component component : components) {
            Component realComponent;
            if (component.getId() == null) {
                realComponent = new Component();
            }
            else {
                realComponent = cm.getComponent(component.getId());
            }

            realComponent.setName(component.getName());
            cm.saveOrUpdate(realComponent);
        }

        // Then, if the user checked anyone off to be deleted, delete them
        if (deleteIds != null) {
            for (int id : deleteIds) {
                cm.deleteComponent(id);
            }
        }

        return new RedirectResolution("/bugzooky/AdministerBugzooky.jsp");
    }
}
