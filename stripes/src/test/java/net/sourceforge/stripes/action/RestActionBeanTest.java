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
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This is a series of tests for Stripes REST action beans.
 */
@RestActionBean
@UrlBinding("/test")
public class RestActionBeanTest extends FilterEnabledTestBase implements ActionBean {

    @Validate(on = "head", required = true)
    private String id;

    public Resolution customHttpVerb() {
        return new JsonResolution("Yay!  This is a custom HTTP verb!");
    }

    public Resolution get() {
        Map< String, Object> response = new HashMap< String, Object>();
        response.put("foo", "bar");
        response.put("hello", "world");

        Map< String, Number> nested = new HashMap< String, Number>();
        nested.put("one", 1);
        nested.put("two", 2);

        response.put("numbers", nested);

        return new JsonResolution(response);
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

    private ActionBeanContext context;

    public ActionBeanContext getContext() {
        return this.context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    @Test(groups = "fast")
    public void testGetAttemptOnPostMethod() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.getRequest().setMethod("GET");
        trip.execute("onlySupportsPost");
        Assert.assertEquals(trip.getResponse().getStatus(), HttpURLConnection.HTTP_BAD_METHOD);
    }

    @Test(groups = "fast")
    public void testPostAttemptOnPostMethod() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.getRequest().setMethod("POST");
        trip.execute("onlySupportsPost");
        Assert.assertEquals(trip.getResponse().getStatus(), HttpURLConnection.HTTP_OK);
    }

    @Test(groups = "fast")
    public void testSuccessfulGet() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.getRequest().setMethod("GET");
        trip.execute();
        Assert.assertEquals(trip.getResponse().getStatus(), HttpURLConnection.HTTP_OK);
    }

    @Test(groups = "fast")
    public void testFailedPost() throws Exception {
        // Since no event is specified, this should default to the post() method and attempt
        // to execute it.  Since one doesn't exist, it should throw a 404.
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.getRequest().setMethod("POST");
        trip.execute();
        Assert.assertEquals(trip.getResponse().getStatus(), HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test(groups = "fast")
    public void testMissingRequiredParameterOnHead() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.getRequest().setMethod("HEAD");
        trip.execute();
        Assert.assertTrue(trip.getValidationErrors().hasFieldErrors() && trip.getValidationErrors().size() == 1);
    }

    @Test(groups = "fast")
    public void testFailedCustomValidationOnHead() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.setParameter("id", "SOME_ID");
        trip.getRequest().setMethod("HEAD");
        trip.execute();
        Assert.assertTrue(!trip.getValidationErrors().hasFieldErrors() && trip.getValidationErrors().size() == 1);
    }

    @Test(groups = "fast")
    public void testUnhandledException() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.execute("testUnhandledExceptionEvent");
        Assert.assertEquals(trip.getResponse().getStatus(), HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    @Test(groups = "fast")
    public void testCustomHTTPVerb() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.getRequest().setMethod("customHttpVerb");
        trip.execute();
        Assert.assertEquals(trip.getResponse().getStatus(), HttpURLConnection.HTTP_OK);
    }

    private void logTripResponse(MockRoundtrip trip) {
        System.out.println("TRIP RESPONSE: [Event=" + trip.getActionBean(getClass()).getContext().getEventName() + "] [Status=" + trip.getResponse().getStatus()
                + "] [Message=" + trip.getResponse().getOutputString() + "] [Error Message="
                + trip.getResponse().getErrorMessage() + "]");
    }

}
