package net.sourceforge.stripes.examples.bugzooky;

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.HtmlUtil;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * ActionBean that is used to display source files from the bugzooky web application
 * to the user.
 *
 * @author Tim Fennell
 */
public class ViewResourceActionBean extends BugzookyActionBean {
    @Validate(required=true)
    private String resource;

    /** Sets the name resource to be viewed. */
    public void setResource(String resource) { this.resource = resource; }

    /** Gets the name of the resource to be viewed. */
    public String getResource() { return resource; }

    /** Validates that only resources in the allowed places are asked for. */
    @ValidationMethod
    public void validate(ValidationErrors errors) {
        if (resource.startsWith("/WEB-INF") && !resource.startsWith("/WEB-INF/src")) {
            errors.add("resource",
                       new SimpleError("Naughty, naughty. We mustn't hack the URL now."));
        }
    }

    /**
     * Handler method which will handle a request for a resource in the web application
     * and stream it back to the client inside of an HTML preformatted section.
     */
    public Resolution view() {
        final InputStream stream = getContext().getRequest().getSession()
                                  .getServletContext().getResourceAsStream(this.resource);
        final BufferedReader reader = new BufferedReader( new InputStreamReader(stream) );

        return new Resolution() {
            public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
                PrintWriter writer = response.getWriter();
                writer.write("<html><head><title>");
                writer.write(resource);
                writer.write("</title></head><body><pre>");

                String line;
                while ( (line = reader.readLine()) != null ) {
                    writer.write(HtmlUtil.encode(line));
                    writer.write("\n");
                }

                writer.write("</pre></body></html>");
            }
        };
    }

    /**
     * Method used when this ActionBean is used as a view helper. Returns a listing of all the
     * JSPs and ActionBeans available for viewing.
     */
    public Collection getAvailableResources() {
        ServletContext ctx = getContext().getRequest().getSession().getServletContext();
        SortedSet<String> resources = new TreeSet<String>();
        resources.addAll( ctx.getResourcePaths("/bugzooky/"));
        resources.addAll( ctx.getResourcePaths("/bugzooky/layout/"));
        resources.addAll( ctx.getResourcePaths("/WEB-INF/src/"));

        Iterator<String> iterator = resources.iterator();
        while (iterator.hasNext()) {
            String file = iterator.next();
            if (!file.endsWith(".jsp") && !file.endsWith(".java")) {
                iterator.remove();
            }
        }

        return resources;
    }
}
