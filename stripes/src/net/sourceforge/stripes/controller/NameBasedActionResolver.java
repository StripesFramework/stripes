/* Copyright (C) 2006 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Literal;
import net.sourceforge.stripes.util.Log;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;

/**
 * <p>An ActionResolver that uses the names of classes and methods to generate sensible default
 * URL bindings and event names respectively. Extends the default
 * {@link AnnotatedClassActionResolver}, and is fully backward compatible.  Any classes and
 * methods that are annotated with {@link net.sourceforge.stripes.action.UrlBinding} and
 * {@link net.sourceforge.stripes.action.HandlesEvent} will retain the bindings specified in
 * those annotations.  In the case when an annotation is absent then a default binding is
 * generated.</p>
 *
 * <p>The generation of ActionBean URL bindings is done by taking the class name and removing
 * any extraneous packages at the front of the name, removing the strings "Action" and "Bean"
 * from the end of the name, substituting slashes for periods and appending a suffix (.action by
 * default).  The set of packages that are trimmed is specified by the
 * {@code getBasePackages()} method.  By default this method returns the set
 * [web, www, stripes, action].  These packages (and their parents) are removed from the
 * class name. E.g. {@code com.myco.web.foo.BarActionBean} would become {@code foo.BarActionBean}.
 * Continuing on, we would trim this further to {@code foo.Bar} and then translate it to
 * {@code /foo/Bar}.  Lastly the suffix returned by {@code getBindingSuffix()} is appended
 * giving the binding {@code /foo/Bar.action}.</p>
 *
 * <p>The translation of class names into URL bindings is designed to be easy to override and
 * customize.  To that end you can easiliy change how this translation is done by overriding
 * {@code getBasePackages()} and/or {@code getBindingSuffix()}, or completely customize the
 * behaviour by overriding {@code getUrlBinding(String)}.</p>
 *
 * <p>Mapping of method names to event names is simpler.  Again the parent class is delegated to
 * in case the method is annotated. If it is not, and the method is a concrete public method that
 * returns a Resolution (or subclass thereof) it is mapped to an event of the same name as the
 * method.  So an un-annotated method "{@code public Resolution view()}" is mapped to an event
 * called "view".  It should be noted that there is no special method name that signifies the
 * default handler.  If there is more than one handler and you require a default handler you
 * must still mark the default method with {@code @DefaultHandler}.</p>
 *
 * <p>Another useful feature of the NameBasedActionResolver is that when a request arrives for a
 * URL that is not bound to an ActionBean the resolver will attempt to map the request to a view
 * and return a 'dummy' ActionBean that will take the user to the view.  The exact behaviour is
 * modifiable by overriding one or more of
 * {@link #handleActionBeanNotFound(ActionBeanContext, String)} or {@link #findView(String)}. The
 * default behaviour is to map the URL being requested to three potential JSP names/paths, check
 * for the existence of a JSP at those locations and if one exists then to return an ActionBean
 * that will render the view.  For example if a user requsted '/account/ViewAccount.action' but
 * an ActionBean does not yet exist bound to that URL, the resolver will check for JSPs in the
 * following order:</p>
 *
 * <ul>
 *   <li>/account/ViewAccount.jsp</li>
 *   <li>/account/viewAccount.jsp</li>
 *   <li>/account/view_account.jsp</li>
 * </ul>
 *
 * <p>The value of this approach comes from the fact that by default all pages can appear to have
 * a pre-action whether they actually have one or not.  In the above can you might chose to link
 * to {@code /account/ViewAccount.action} even though you know that no action exists and you want
 * to navigate directly to a page. This way, if you later decide you do need a pre-action for any
 * reason you can simply code the ActionBean and be done.  No URLs or links need to be modified
 * and all requests to {@code /account/ViewAccount.action} will flow through the ActionBean.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.2
 */
public class NameBasedActionResolver extends AnnotatedClassActionResolver {
    /**
     * Default set of packages (web, www, stripes, action) to be removed from the front
     * of class names when translating them to URL bindings.
     */
    public static final Set<String> BASE_PACKAGES =
            Collections.unmodifiableSet(Literal.set("web", "www", "stripes", "action"));

    /** Default suffix (.action) to add to URL bindings.*/
    public static final String DEFAULT_BINDING_SUFFIX = ".action";

    /** Log instance used to log information from this class. */
    private static final Log log = Log.getInstance(NameBasedActionResolver.class);

    /**
     * First invokes the parent classes init() method and then quietly adds a specialized
     * ActionBean to the set of ActionBeans the resolver is managing.  The "specialized" bean
     * is one that is used when a bean is not bound to a URL, to then forward the user to
     * an appropriate view if one exists.
     */
    @Override
    public void init(Configuration configuration) throws Exception {
        super.init(configuration);
        addActionBean(DefaultViewActionBean.class);
    }

    /**
     * <p>Finds or generates the URL binding for the class supplied. First delegates to the parent
     * class to see if an annotated url binding is present. If not, the class name is taken
     * and translated into a URL binding using {@code getUrlBinding(String name)}.</p>
     *
     * @param clazz a Class represenging an ActionBean
     * @return the String URL binding for the ActionBean
     */
    @Override
    public String getUrlBinding(Class<? extends ActionBean> clazz) {
        String binding = super.getUrlBinding(clazz);

        // If there's no annotated binding, and the class is concrete
        if (binding == null && !Modifier.isAbstract(clazz.getModifiers())) {
            binding = getUrlBinding(clazz.getName());
        }

        return binding;
    }

    /**
     * Takes a class name and translats it into a URL binding by removing extraneous package names,
     * removing Action, Bean, or ActionBean from the end of the class name if present, replacing
     * periods with slashes, and appending a standard suffix as supplied by
     * {@link net.sourceforge.stripes.controller.NameBasedActionResolver#getBindingSuffix()}.</p>
     *
     * <p>For example the class {@code com.myco.web.action.user.RegisterActionBean} would be
     * translated to {@code /user/Register.action}.  The behaviour of this method can be
     * overridden either directly or by overriding the methods {@code getBindingSuffix()} and
     * {@code getBasePackages()} which are used by this method.</p>
     *
     * @param name the name of the class to create a binding for
     * @return a String URL binding for the class
     *
     */
    protected String getUrlBinding(String name) {
        // Chop off the packages up until (and including) any base package
        for (String base : getBasePackages()) {
            int i = name.indexOf(base);
            if (i != -1) {
                name = name.substring(i + base.length());
            }
        }

        // If it ends in Action or Bean (or ActionBean) take that off
        if (name.endsWith("Bean")) {
            name = name.substring(0, name.length() - 4);
        }
        if (name.endsWith("Action")) {
            name = name.substring(0, name.length() - 6);
        }

        // Replace periods with slashes and make sure it starts with one
        name = name.replace('.', '/');
        if (!name.startsWith("/")) {
            name = "/" + name;
        }

        // Lastly add the suffix
        name += getBindingSuffix();
        return name;
    }

    /**
     * Returns a set of package names (fully qualified or not) that should be removed
     * from the start of a classname before translating the name into a URL Binding. By default
     * returns "web", "www", "stripes" and "action".
     *
     * @return a non-null set of String package names.
     */
    protected Set<String> getBasePackages() {
        return BASE_PACKAGES;
    }

    /**
     * Returns a non-null String suffix to be used when constructing URL bindings. The
     * default is ".action".
     */
    protected String getBindingSuffix() {
        return DEFAULT_BINDING_SUFFIX;
    }

    /**
     * First checks with the super class to see if an annotated event name is present, and if
     * not then returns the name of the handler method itself.  Will return null for methods
     * that do not return a resolution or are non-public or abstract.
     *
     * @param handler a method which may or may not be a handler method
     * @return the name of the event handled, or null
     */
    @Override
    public String getHandledEvent(Method handler) {
        String name = super.getHandledEvent(handler);

        // If the method isn't annotated, but does return a resolution and is
        // not abstract (we already know it's public) then use the method name
        if ( name == null && !Modifier.isAbstract(handler.getModifiers())
                && Resolution.class.isAssignableFrom(handler.getReturnType()) ) {

            name = handler.getName();
        }

        return name;
    }

    /**
     * <p>Overridden to trap the exception that is thrown when a URL cannot be mapped to an
     * ActionBean and then attempt to construct a dummy ActionBean that will forward the
     * user to an appropriate view.  In an exception is caught then the method
     * {@link #handleActionBeanNotFound(ActionBeanContext, String)} is invoked to handle
     * the exception.</p>
     *
     * @param context the ActionBeanContext of the current request
     * @param urlBinding the urlBinding determined for the current request
     * @return an ActionBean if there is an appropriate way to handle the request
     * @throws StripesServletException if no ActionBean or alternate strategy can be found
     */
    @Override
    public ActionBean getActionBean(ActionBeanContext context,
                                    String urlBinding) throws StripesServletException {
        try {
            return super.getActionBean(context, urlBinding);
        }
        catch (StripesServletException sse) {
            ActionBean bean = handleActionBeanNotFound(context, urlBinding);
            if (bean != null) {
                return bean;
            }
            else {
                throw sse;
            }
        }
    }

    /**
     * Invoked when no appropriate ActionBean can be located. Attempts to locate a view that is
     * appropriate for this request by calling {@link #findView(String)}.  If a view is found
     * then a dummy ActionBean is constructed that will send the user to the view. If no appropriate
     * view is found then null is returned.
     *
     * @param context the ActionBeanContext of the current request
     * @param urlBinding the urlBinding determined for the current request
     * @return an ActionBean that will render a view for the user, or null
     * @since Stripes 1.3
     */
    protected ActionBean handleActionBeanNotFound(ActionBeanContext context, String urlBinding) {
        ActionBean bean = null;
        Resolution view = findView(urlBinding);

        if (view != null) {
            log.debug("Could not find an ActionBean bound to '", urlBinding, "', but found a view ",
                      "at '", view, "'. Forwarding the user there instead.");
            bean = new DefaultViewActionBean(view);
        }

        return bean;
    }

    /**
     * <p>Attempts to locate a default view for the urlBinding provided and return a
     * ForwardResolution that will take the user to the view.  Looks for views by
     * converting the incoming urlBinding with the following rules.  For example if the
     * urlBinding is '/account/ViewAccount.action' the following views will be looked for
     * in order:</p>
     *
     * <ul>
     *   <li>/account/ViewAccount.jsp</li>
     *   <li>/WEB-INF/account/ViewAccount.jsp</li>
     *   <li>/account/view_account.jsp</li>
     *   <li>/WEB-INF/account/view_account.jsp</li>
     * </ul>
     *
     * <p>For each JSP name derived a check is performed using
     * {@link ServletContext#getResource(String)} to see if there is a JSP located at that URL.
     * Only if a JSP actually exists will a Resolution be returned.</p>
     *
     * <p>Can be overridden to look for JSPs with a different pattern, or to provide a different
     * kind of resolution.  It is strongly recommended when overriding this method to check for
     * the actual existence of views prior to manufacturing a resolution in order not to cause
     * confusion when URLs are mis-typed.</p>
     *
     * @param urlBinding the url being accessed by the client in the current request
     * @return a Resolution if a default view can be found, or null otherwise
     * @since Stripes 1.3
     */
    protected Resolution findView(String urlBinding) {
        String path = urlBinding.substring(0, urlBinding.lastIndexOf("/") + 1);
        String name = urlBinding.substring(path.length(), urlBinding.lastIndexOf("."));
        ServletContext ctx = StripesFilter.getConfiguration()
                .getBootstrapPropertyResolver().getFilterConfig().getServletContext();

        try {
            // This will try /account/ViewAccount.jsp
            String jsp = path + name + ".jsp";
            if (ctx.getResource(jsp) != null) {
                return new ForwardResolution(jsp);
            }

            // This will try /account/viewAccount.jsp
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            jsp = path + name + ".jsp";
            if (ctx.getResource(jsp) != null) {
                return new ForwardResolution(jsp);
            }

            // And finally this will try /account/view_account.jsp
            StringBuilder builder = new StringBuilder();
            for (int i=0; i<name.length(); ++i) {
                char ch = name.charAt(i);
                if (Character.isUpperCase(ch)) {
                    builder.append("_");
                    builder.append(Character.toLowerCase(ch));
                }
                else {
                    builder.append(ch);
                }
            }

            jsp = path + builder.toString() + ".jsp";
            if (ctx.getResource(jsp) != null) {
                return new ForwardResolution(jsp);
            }

            return null;
        }
        catch (MalformedURLException mue) {
            return null;
        }
    }
}

/**
 * <p>A special purpose ActionBean that is used by the NameBasedActionResolver when a valid
 * ActionBean cannot be found for a URL.  If the URL can be successfully translated into a
 * JSP URL and a JSP exists, an instance of this ActionBean is created that will forward the
 * user to the appropriate JSP.</p>
 *
 * <p>Because this ActionBean does not have a default no-arg constructor, even though it
 * gets bound to a URL, if that URL is hit the ActionBean cannot be instantiated and therefore
 * cannot be accessed directly by a user playing with the URL.</p>
 *
 * @author Tim Fennell, Abdullah Jibaly
 * @since Stripes 1.3
 */
class DefaultViewActionBean implements ActionBean {
    private ActionBeanContext context;
    private Resolution view;

    public DefaultViewActionBean(Resolution view) { this.view = view; }
    public void setContext(ActionBeanContext context) { this.context = context; }
    public ActionBeanContext getContext() { return this.context; }

    public Resolution view() { return view; }
}
