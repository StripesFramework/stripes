/*
 * Copyright 2014 Rick Grashel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.action;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * This is a series of tests for Stripes REST action beans.
 */
@RestActionBean
@UrlBinding("/test/custom-validation-error-handler/{person}")
public class RestWithCustomValidationErrorHandlerActionBeanTest extends FilterEnabledTestBase implements ActionBean, ValidationErrorHandler {

    private static final Log log = Log.getInstance(RestWithCustomValidationErrorHandlerActionBeanTest.class);

    @Validate(on = "head", required = true)
    private String id;

    @ValidateNestedProperties( {
        @Validate( field = "id", required = true, on = "boundPersonEvent" )
    })
    @Validate(converter = PersonTypeConverter.class)
    private Person person;

    @Override
    public Resolution handleValidationErrors( ValidationErrors errors ) throws Exception {
        return new ErrorResolution( HttpServletResponse.SC_BAD_REQUEST, "yay! custom error validation handler" );
    }

    public Resolution customHttpVerb() {
        return new JsonResolution("Yay!  This is a custom HTTP verb!");
    }

    public Resolution get() {
        Map< String, Object> response = new HashMap< String, Object>();
        response.put("foo", "bar");
        response.put("hello", "world");
        response.put("person", new Person());

        Map< String, Number> nested = new HashMap< String, Number>();
        nested.put("one", 1);
        nested.put("two", 2);

        response.put("numbers", nested);

        return new JsonResolution(response);
    }

    @POST
    public Resolution boundPersonEvent() {
        return new JsonResolution(getPerson());
    }

    public Resolution jsonResolutionWithExclusion() {
        Person p = new Person();
        return new JsonResolution(p, "firstName");
    }

    @ValidationMethod(on = "head")
    public void validateHeadCall(ValidationErrors errors) {
        errors.addGlobalError(new SimpleError("The head request was not valid for whatever custom reason."));
    }

    public Resolution head() {
        return new JsonResolution("Successful head!");
    }

    public Resolution testUnhandledExceptionEvent() {
        throw new RuntimeException("Some Unhandled Exception Occurred!");
    }

    @POST
    public Resolution onlySupportsPost() {
        return new JsonResolution("Successful onlySupportsPost()!");
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return this.person;
    }

    private ActionBeanContext context;

    public ActionBeanContext getContext() {
        return this.context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    @Test(groups = "fast")
    public void testCustomValidationError() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.getRequest().setMethod("HEAD");
        trip.execute("head");
        Assert.assertEquals(trip.getResponse().getStatus(), HttpURLConnection.HTTP_BAD_REQUEST);
        Assert.assertEquals(trip.getResponse().getErrorMessage(), "yay! custom error validation handler");
        logTripResponse(trip);
    }

    private void logTripResponse(MockRoundtrip trip) {
        log.debug("TRIP RESPONSE: [Event=" + trip.getActionBean(getClass()).getContext().getEventName() + "] [Status=" + trip.getResponse().getStatus()
                + "] [Message=" + trip.getResponse().getOutputString() + "] [Error Message="
                + trip.getResponse().getErrorMessage() + "]");
    }

    public static class Person {

        String id = null;
        String firstName = "John";
        String lastName = "Doe";
        List< String> favoriteFoods = new ArrayList< String>();
        List< Person> children = new ArrayList<Person>();

        public void setChildren( List< Person > children ) {
            this.children = children;
        }

        public List< Person > getChildren() {
            return this.children;
        }

        public void setId( String id ) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        public void setFavoriteFoods(List< String> favoriteFoods) {
            this.favoriteFoods = favoriteFoods;
        }

        public List< String> getFavoriteFoods() {
            return this.favoriteFoods;
        }

        public String getFirstName() {
            return this.firstName;
        }

        public String getLastName() {
            return this.lastName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

    public static class PersonTypeConverter implements TypeConverter<Person> {

        public void setLocale(Locale locale) {
        }

        public Locale getLocale() {
            return Locale.getDefault();
        }

        public Person convert(String input, Class<? extends Person> targetType, Collection<ValidationError> errors) {
            Person p = new Person();
            p.setId(input);
            return p;
        }
    }
}
