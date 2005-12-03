package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.examples.bugzooky.biz.PersonManager;
import net.sourceforge.stripes.examples.bugzooky.biz.Person;

/**
 *
 */
@UrlBinding("/bugzooky/Login.action")
public class LoginActionBean extends BugzookyActionBean {
    private String username;
    private String password;
    private String targetUrl;

    /** The username of the user trying to log in. */
    @Validate(required=true)
    public void setUsername(String username) { this.username = username; }

    /** The username of the user trying to log in. */
    public String getUsername() { return username; }

    /** The password of the user trying to log in. */
    @Validate(required=true)
    public void setPassword(String password) { this.password = password; }

    /** The password of the user trying to log in. */
    public String getPassword() { return password; }

    /** The URL the user was trying to access (null if the login page was accessed directly). */
    public String getTargetUrl() { return targetUrl; }

    /** The URL the user was trying to access (null if the login page was accessed directly). */
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }


    @DefaultHandler @HandlesEvent("Login")
    public Resolution login() {
        PersonManager pm = new PersonManager();
        Person person = pm.getPerson(this.username);

        if (person == null) {
            ValidationError error = new LocalizableError
                    ("/bugzooky/Login.action.usernameDoesNotExist", username);
            getContext().getValidationErrors().add("username", error);
            return getContext().getSourcePageResolution();
        }
        else if (!person.getPassword().equals(password)) {
            ValidationError error = new LocalizableError
                    ("/bugzooky/Login.action.incorrectPassword");
            getContext().getValidationErrors().add("password", error);
            return getContext().getSourcePageResolution();
        }
        else {
            getContext().setUser(person);
            if (this.targetUrl != null) {
                return new RedirectResolution(this.targetUrl);
            }
            else {
                return new RedirectResolution("/bugzooky/BugList.jsp");
            }
        }
    }
}
