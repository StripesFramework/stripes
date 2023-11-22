/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.controller;

import jakarta.servlet.ServletContext;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Literal;
import net.sourceforge.stripes.util.Log;

/**
 * An ActionResolver that uses the names of classes and methods to generate sensible default URL
 * bindings and event names respectively. Extends the default {@link AnnotatedClassActionResolver},
 * and is fully backward compatible. Any classes and methods that are annotated with {@link
 * net.sourceforge.stripes.action.UrlBinding} and {@link
 * net.sourceforge.stripes.action.HandlesEvent} will retain the bindings specified in those
 * annotations. In the case when an annotation is absent then a default binding is generated.
 *
 * <p>The generation of ActionBean URL bindings is done by taking the class name and removing any
 * extraneous packages at the front of the name, removing the strings "Action" and "Bean" from the
 * end of the name, substituting slashes for periods and appending a suffix (.action by default).
 * The set of packages that are trimmed is specified by the {@code getBasePackages()} method. By
 * default, this method returns the set [web, www, stripes, action]. These packages (and their
 * parents) are removed from the class name. E.g. {@code com.myco.web.foo.BarActionBean} would
 * become {@code foo.BarActionBean}. Continuing on, the list of Action Bean suffixes, specified by
 * the {@code getActionBeanSuffixes()} method, are trimmed from the end of the Action Bean class
 * name. With the defaults, [Bean, Action], we would trim {@code foo.BarActionBean} further to
 * {@code foo.Bar}, and then translate it to {@code /foo/Bar}. Lastly the suffix returned by {@code
 * getBindingSuffix()} is appended, giving the binding {@code /foo/Bar.action}.
 *
 * <p>The translation of class names into URL bindings is designed to be easy to override and
 * customize. To that end you can easily change how this translation is done by overriding {@code
 * getBasePackages()} and/or {@code getBindingSuffix()}, or completely customize the behaviour by
 * overriding {@code getUrlBinding(String)}.
 *
 * <p>Mapping of method names to event names is simpler. Again, the parent class is delegated to in
 * case the method has an annotation. If it is not, and the method is a concrete public method that
 * returns a Resolution (or subclass thereof) it is mapped to an event of the same name as the
 * method. So an un-annotated method "{@code public Resolution view()}" is mapped to an event called
 * "view". It should be noted that there is no special method name that signifies the default
 * handler. If there is more than one handler, and you require a default handler you must still mark
 * the default method with {@code @DefaultHandler}.
 *
 * <p>Another useful feature of the NameBasedActionResolver is that when a request arrives for a URL
 * that is not bound to an ActionBean the resolver will attempt to map the request to a view and
 * return a 'dummy' ActionBean that will take the user to the view. The exact behaviour is
 * modifiable by overriding one or more of {@link #handleActionBeanNotFound(ActionBeanContext,
 * String)}, {@link #findView(String)} or {@link #getFindViewAttempts(String)}. The default
 * behaviour is to map the URL being requested to three potential JSP names/paths, check for the
 * existence of a JSP at those locations and if one exists then to return an ActionBean that will
 * render the view. For example if a user requested '/account/ViewAccount.action' but an ActionBean
 * does not yet exist bound to that URL, the resolver will check for JSPs in the following order:
 *
 * <ul>
 *   <li>/account/ViewAccount.jsp
 *   <li>/account/viewAccount.jsp
 *   <li>/account/view_account.jsp
 * </ul>
 *
 * <p>The value of this approach comes from the fact that by default all pages can appear to have a
 * pre-action whether they actually have one or not. In the above can you might choose to link to
 * {@code /account/ViewAccount.action} even though you know that no action exists, and you want to
 * navigate directly to a page. This way, if you later decide you do need a pre-action for any
 * reason you can simply code the ActionBean and be done. No URLs or links need to be modified and
 * all requests to {@code /account/ViewAccount.action} will flow through the ActionBean.
 *
 * @author Tim Fennell
 * @since Stripes 1.2
 */
public class NameBasedActionResolver extends AnnotatedClassActionResolver {
  /**
   * Default set of packages (web, www, stripes, action) to be removed from the front of class names
   * when translating them to URL bindings.
   */
  public static final Set<String> BASE_PACKAGES =
      Collections.unmodifiableSet(Literal.set("web", "www", "stripes", "action"));

  /** Default suffix (.action) to add to URL bindings. */
  public static final String DEFAULT_BINDING_SUFFIX = ".action";

  /** Default list of suffixes (Bean, Action) to remove to the end of the Action Bean class name. */
  public static final List<String> DEFAULT_ACTION_BEAN_SUFFIXES =
      Collections.unmodifiableList(Literal.list("Bean", "Action"));

  /** Log instance used to log information from this class. */
  private static final Log log = Log.getInstance(NameBasedActionResolver.class);

  /**
   * First invokes the parent classes init() method and then quietly adds a specialized ActionBean
   * to the set of ActionBeans the resolver is managing. The "specialized" bean is one that is used
   * when a bean is not bound to a URL, to then forward the user to an appropriate view if one
   * exists.
   */
  @Override
  public void init(Configuration configuration) throws Exception {
    super.init(configuration);
    addActionBean(DefaultViewActionBean.class);
  }

  /**
   * Finds or generates the URL binding for the class supplied. First delegates to the parent class
   * to see if an annotated url binding is present. If not, the class name is taken and translated
   * into a URL binding using {@code getUrlBinding(String name)}.
   *
   * @param clazz a Class representing an ActionBean
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
   * Takes a class name and translates it into a URL binding by removing extraneous package names,
   * removing Action, Bean, or ActionBean from the end of the class name if present, replacing
   * periods with slashes, and appending a standard suffix as supplied by {@link
   * net.sourceforge.stripes.controller.NameBasedActionResolver#getBindingSuffix()}.
   *
   * <p>For example the class {@code com.myco.web.action.user.RegisterActionBean} would be
   * translated to {@code /user/Register.action}. The behaviour of this method can be overridden
   * either directly or by overriding the methods {@code getBindingSuffix()} and {@code
   * getBasePackages()} which are used by this method.
   *
   * @param name the name of the class to create a binding for
   * @return a String URL binding for the class
   */
  protected String getUrlBinding(String name) {
    // Chop off the packages up until (and including) any base package
    for (String base : getBasePackages()) {
      int i = name.indexOf("." + base + ".");
      if (i != -1) {
        name = name.substring(i + base.length() + 1);
      } else if (name.startsWith(base + ".")) {
        name = name.substring(base.length());
      }
    }

    // If it ends in any of the Action Bean suffixes, remove them
    for (String suffix : getActionBeanSuffixes()) {
      if (name.endsWith(suffix)) {
        name = name.substring(0, name.length() - suffix.length());
      }
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
   * Returns a set of package names (fully qualified or not) that should be removed from the start
   * of a classname before translating the name into a URL Binding. By default, returns "web",
   * "www", "stripes" and "action".
   *
   * @return a non-null set of String package names.
   */
  protected Set<String> getBasePackages() {
    return BASE_PACKAGES;
  }

  /**
   * Returns a non-null String suffix to be used when constructing URL bindings. The default is
   * ".action".
   */
  protected String getBindingSuffix() {
    return DEFAULT_BINDING_SUFFIX;
  }

  /**
   * Returns a list of suffixes to be removed from the end of the Action Bean class name, if
   * present. The defaults are ["Bean", "Action"].
   *
   * @since Stripes 1.5
   */
  protected List<String> getActionBeanSuffixes() {
    return DEFAULT_ACTION_BEAN_SUFFIXES;
  }

  /**
   * First checks with the super class to see if an annotated event name is present, and if not then
   * returns the name of the handler method itself. Will return null for methods that do not return
   * a resolution or are non-public or abstract.
   *
   * @param handler a method which may or may not be a handler method
   * @return the name of the event handled, or null
   */
  @Override
  public String getHandledEvent(Method handler) {
    String name = super.getHandledEvent(handler);

    // If the method isn't annotated, but does return a resolution and is
    // not abstract (we already know it's public) then use the method name
    if (name == null
        && !Modifier.isAbstract(handler.getModifiers())
        && Resolution.class.isAssignableFrom(handler.getReturnType())
        && handler.getParameterTypes().length == 0) {

      name = handler.getName();
    }

    return name;
  }

  /**
   * Overridden to trap the exception that is thrown when a URL cannot be mapped to an ActionBean
   * and then attempt to construct a dummy ActionBean that will forward the user to an appropriate
   * view. In an exception is caught then the method {@link
   * #handleActionBeanNotFound(ActionBeanContext, String)} is invoked to handle the exception.
   *
   * @param context the ActionBeanContext of the current request
   * @param urlBinding the urlBinding determined for the current request
   * @return an ActionBean if there is an appropriate way to handle the request
   * @throws StripesServletException if no ActionBean or alternate strategy can be found
   */
  @Override
  public ActionBean getActionBean(ActionBeanContext context, String urlBinding)
      throws StripesServletException {
    try {
      return super.getActionBean(context, urlBinding);
    } catch (StripesServletException sse) {
      ActionBean bean = handleActionBeanNotFound(context, urlBinding);
      if (bean != null) {
        setActionBeanContext(bean, context);
        assertGetContextWorks(bean);
        return bean;
      } else {
        throw sse;
      }
    }
  }

  /**
   * Invoked when no appropriate ActionBean can be located. Attempts to locate a view that is
   * appropriate for this request by calling {@link #findView(String)}. If a view is found then a
   * dummy ActionBean is constructed that will send the user to the view. If no appropriate view is
   * found then null is returned.
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
      log.debug(
          "Could not find an ActionBean bound to '",
          urlBinding,
          "', but found a view ",
          "at '",
          view,
          "'. Forwarding the user there instead.");
      bean = new DefaultViewActionBean(view);
    }

    return bean;
  }

  /**
   * Attempts to locate a default view for the urlBinding provided and return a ForwardResolution
   * that will take the user to the view. Looks for views by using the list of attempts returned by
   * {@link #getFindViewAttempts(String)}.
   *
   * <p>For each view name derived a check is performed using {@link
   * ServletContext#getResource(String)} to see if there is a file located at that URL. Only if a
   * file actually exists will a Resolution be returned.
   *
   * <p>Can be overridden to provide a different kind of resolution. It is strongly recommended when
   * overriding this method to check for the actual existence of views prior to manufacturing a
   * resolution in order not to cause confusion when URLs are mistyped.
   *
   * @param urlBinding the url being accessed by the client in the current request
   * @return a Resolution if a default view can be found, or null otherwise
   * @since Stripes 1.3
   */
  protected Resolution findView(String urlBinding) {
    List<String> attempts = getFindViewAttempts(urlBinding);

    ServletContext ctx =
        StripesFilter.getConfiguration()
            .getBootstrapPropertyResolver()
            .getFilterConfig()
            .getServletContext();

    for (String jsp : attempts) {
      try {
        // This will try /account/ViewAccount.jsp
        if (ctx.getResource(jsp) != null) {
          return new ForwardResolution(jsp);
        }
      } catch (MalformedURLException ignored) {
      }
    }
    return null;
  }

  /**
   * Returns the list of attempts to locate a default view for the urlBinding provided. Generates
   * attempts for views by converting the incoming urlBinding with the following rules. For example
   * if the urlBinding is '/account/ViewAccount.action' the following views will be returned in
   * order:
   *
   * <ul>
   *   <li>/account/ViewAccount.jsp
   *   <li>/account/viewAccount.jsp
   *   <li>/account/view_account.jsp
   * </ul>
   *
   * <p>Can be overridden to look for views with a different pattern.
   *
   * @param urlBinding the url being accessed by the client in the current request
   * @since Stripes 1.5
   */
  protected List<String> getFindViewAttempts(String urlBinding) {
    List<String> attempts = new ArrayList<>(3);

    int lastPeriod = urlBinding.lastIndexOf('.');
    String path = urlBinding.substring(0, urlBinding.lastIndexOf("/") + 1);
    String name =
        (lastPeriod >= path.length())
            ? urlBinding.substring(path.length(), lastPeriod)
            : urlBinding.substring(path.length());

    if (!name.isEmpty()) {
      // This will try /account/ViewAccount.jsp
      attempts.add(path + name + ".jsp");

      // This will try /account/viewAccount.jsp
      name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
      attempts.add(path + name + ".jsp");

      // And finally this will try /account/view_account.jsp
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < name.length(); ++i) {
        char ch = name.charAt(i);
        if (Character.isUpperCase(ch)) {
          builder.append("_");
          builder.append(Character.toLowerCase(ch));
        } else {
          builder.append(ch);
        }
      }
      attempts.add(path + builder + ".jsp");
    }

    return attempts;
  }

  /**
   * In addition to the {@link net.sourceforge.stripes.action.ActionBean} class simple name, also
   * add aliases for shorthand names. For instance, ManageUsersActionBean would get:
   *
   * <ul>
   *   <li>ManageUsersActionBean (simple name)
   *   <li>ManageUsersAction
   *   <li>ManageUsers
   * </ul>
   */
  @Override
  protected void addBeanNameMappings() {
    super.addBeanNameMappings();

    Set<String> generatedAliases = new HashSet<>();
    Set<String> duplicateAliases = new HashSet<>();
    for (Class<? extends ActionBean> clazz : getActionBeanClasses()) {
      String name = clazz.getSimpleName();
      for (String suffix : getActionBeanSuffixes()) {
        if (name.endsWith(suffix)) {
          name = name.substring(0, name.length() - suffix.length());
          if (generatedAliases.contains(name)) {
            log.warn(
                "Found multiple action beans with same bean name ",
                name,
                ". You will need to "
                    + "reference these action beans by their fully qualified names");
            duplicateAliases.add(name);
            continue;
          }

          generatedAliases.add(name);
          actionBeansByName.put(name, clazz);
        }
      }
    }

    // Remove any duplicate aliases that were found
    for (String duplicateAlias : duplicateAliases) {
      actionBeansByName.remove(duplicateAlias);
    }
  }
}
