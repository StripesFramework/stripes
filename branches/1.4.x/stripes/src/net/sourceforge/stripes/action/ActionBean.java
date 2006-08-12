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
package net.sourceforge.stripes.action;

/**
 * <p>Interface for all classes that respond to user interface events. Implementations receive
 * information about the event (usually a form submission) in two ways.  The first is through a
 * ActionBeanContext object which will always be set on the ActionBean prior to any user
 * implemented &quot;handler&quot; methods being invoked.  The second is through the setting of
 * JavaBean style properties on the object (and nested JavaBeans).  Values submitted in an HTML
 * Form will be bound to a property on the object if such a property is defined.  The
 * ActionBeanContext is used primarily to provide access to Servlet APIs such as the request and
 * response, and other information generated during the pre-processing of the request.</p>
 *
 * <p>How Stripes determines which method to invoke is based on Annotations as opposed to a strict
 * interface.  Firstly, each ActionBean should be annotated with a UrlBinding which specifies the
 * path within the web application that the ActionBean will be bound to.  E.g:</p>
 *
 * <pre>
 * &#064;UrlBinding("/action/SampleAction")
 * public SampleActionBean implements ActionBean {
 * ...
 * </pre>
 *
 * <p>At run time Stripes will discover all implementations of ActionBean and, when a form is
 * submitted, will locate the ActionBean to use by matching the path in the request to the
 * UrlBinding annotation on the ActionBean.</p>
 *
 * <p>The way in which a specific event is mapped to a &quot;handler&quot; method can vary.  The
 * default method used by Stripes is to identify the <em>name</em> of the button or image button
 * used to submit the request, and to find the handler method that can handle that event. The way
 * this is declared in the ActionBean is like this:</p>
 *
 * <pre>
 * &#064;HandlesEvent("SampleEvent")
 * public Resolution sample() throws Exception {
 *     ...
 *     return new ForwardResolution("/SampleJsp2.jsp");
 * }
 * </pre>
 *
 * <p>Event names specified in the HandlesEvent annotation should be unique within a given
 * ActionBean (and it's superclasses), but can be re-used across ActionBeans.  For example, a given
 * ActionBean should not have two methods with the annotation <em>&#064;HandlesEvent("Update")</em>,
 * but it would be perfectly reasonable to have two different ActionBeans handle events, from
 * different forms, called Update.</p>
 *
 * <p>It is also possible to designate a method as the default method for handling events in the
 * case that Stripes cannot figure out a specific handler method to invoke.  This occurs most often
 * when a form is submitted by the user hitting the enter/return key, and so no form button is
 * activated.  Essentially the default handler is specifying the default operation for your form.
 * In forms that have only one handler method, that method should alsways be declared as the
 * default.  For example:</p>
 *
 * <pre>
 * &#064;HandlesEvent("Search")
 * &#064;DefaultHandler
 * public Resolution findUsers() throws Exception {
 *     ...
 *     return new ForwardResolution("/SearchResults.jsp");
 * }
 * </pre>
 *
 * <p>Handler methods have two options for what to do when they have finished their processing. The
 * preferred option is to return an instance of an implementation of Resolution.  This keeps things
 * clean and makes it a little easier to change how things work down the road.  The other option
 * is to return nothing, and simply use the Servlet API (available through the ActionBeanContext) to
 * render a response to the user directly.</p>
 *
 * <p>There are two interfaces in the validation package that an ActionBean may optionally
 * implement in order to provide additional functionality.  The first is the
 * {@link net.sourceforge.stripes.validation.Validatable} interface, which allows an ActionBean
 * to perform custom validations after validation and binding has run.  The second is the
 * {@link net.sourceforge.stripes.validation.ValidationErrorHandler} interface which allows the
 * ActionBean to modify what happens when validation errors occur.</p>
 *
 * @see Resolution
 * @see ActionBeanContext
 * @see net.sourceforge.stripes.validation.Validatable
 * @see net.sourceforge.stripes.validation.ValidationErrorHandler
 * @author Tim Fennell
 */
public interface ActionBean {
    /**
     * Called by the Stripes dispatcher to provide context to the ActionBean before invoking the
     * handler method.  Implementations should store a reference to the context for use during
     * event handling.
     *
     * @param context ActionBeanContext associated with the current request
     */
    public void setContext(ActionBeanContext context);

    /**
     * Implementations must implement this method to return a reference to the context object
     * provided to the ActionBean during the call to setContext(ActionBeanContext).
     *
     * @return ActionBeanContext associated with the current request
     */
    public ActionBeanContext getContext();
}
