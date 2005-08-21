package net.sourceforge.stripes.examples.ex1;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Aug 6, 2005 Time: 6:06:40 PM To change this template
 * use File | Settings | File Templates.
 */
@UrlBinding("/action/cat/BulkAdd")
public class BulkAddCatActionBean implements ActionBean {
    private ActionBeanContext context;

    private List<Cat> cats = new ArrayList<Cat>();

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public ActionBeanContext getContext() {
        return this.context;
    }

    public List<Cat> getCats() {
        return this.cats;
    }

    public void setCats(List<Cat> cats) {
        this.cats = cats;
    }

    @DefaultHandler
    public Resolution add() {
        for (Cat cat : this.cats) {
            new CatController().addCat(cat);
        }
        
        return new RedirectResolution("/ex1/CatList.jsp");
    }
}
