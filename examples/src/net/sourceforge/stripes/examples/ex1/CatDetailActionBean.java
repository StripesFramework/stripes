package net.sourceforge.stripes.examples.ex1;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.FormName;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 15, 2005 Time: 7:12:56 PM To change this
 * template use File | Settings | File Templates.
 */
@FormName("ex1/CatDetailsForm")
public class CatDetailActionBean implements ActionBean {
    private static Log log = LogFactory.getLog(CatDetailActionBean.class);

    private ActionBeanContext context;

    private Cat cat;
    private FileBean biography;
    private String inlineBiography;

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public ActionBeanContext getContext() {
        return this.context;
    }

    @ValidateNestedProperties({
        @Validate(field="name", minlength=3),
        @Validate(field="age",  minlength=1, maxlength=2)
    })
    public Cat getCat() {
        return this.cat;
    }

    public void setCat(Cat cat) {
        this.cat = cat;
    }

    public FileBean getBiography() {
        return biography;
    }

    public void setBiography(FileBean biography) {
        this.biography = biography;
    }

    public String getInlineBiography() {
        return inlineBiography;
    }

    public void setInlineBiography(String inlineBiography) {
        this.inlineBiography = inlineBiography;
    }

    @HandlesEvent("Edit")
    public Resolution prepCatForEdit() throws Exception {
        this.cat = new CatController().getCat(this.cat.getName());
        return new ForwardResolution("/ex1/CatDetails.jsp");
    }


    @HandlesEvent("Update")
    public Resolution updateCat() throws Exception {
        new CatController().updateCat(this.cat);

        if (this.biography != null) {
            BufferedReader reader =
                new BufferedReader( new InputStreamReader(this.biography.getInputStream()) );
            StringWriter writer = new StringWriter();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                writer.write(line);
                writer.write("\n");
            }

            log.info( "Biogarphy from file: " + writer.toString() );
        }

        if (this.inlineBiography != null) {
            log.info("Inline Biography: " + this.inlineBiography);
        }

        return new RedirectResolution("/ex1/CatList.jsp");
    }

    @HandlesEvent("New")
    public Resolution getReadyForNewCat() throws Exception {
        return new ForwardResolution("/ex1/CatDetails.jsp");
    }

}
