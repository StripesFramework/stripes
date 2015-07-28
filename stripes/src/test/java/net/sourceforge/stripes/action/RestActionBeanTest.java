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

import java.util.HashMap;
import java.util.Map;
import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.testng.annotations.Test;

/**
 * This is a series of tests for Stripes REST action beans.
 */
@RestActionBean
@UrlBinding("/test")
public class RestActionBeanTest extends FilterEnabledTestBase implements ActionBean {

    @Validate(on = "head", required = true)
    private String id;

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
    public void successfulGet() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.getRequest().setMethod("GET");
        trip.execute();
        logTripResponse(trip);
    }

    @Test(groups = "fast")
    public void failedPost() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.getRequest().setMethod("POST");
        trip.execute();
        logTripResponse(trip);
    }

    @Test(groups = "fast")
    public void missingRequiredParameterOnHead() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.getRequest().setMethod("HEAD");
        trip.execute();
        logTripResponse(trip);
    }

    @Test(groups = "fast")
    public void failedCustomValidationOnHead() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.setParameter("id", "SOME_ID");
        trip.getRequest().setMethod("HEAD");
        trip.execute();
        logTripResponse(trip);
    }

    private void logTripResponse(MockRoundtrip trip) {
        System.out.println("TRIP RESPONSE: [Status=" + trip.getResponse().getStatus() 
                + "] [Message=" + trip.getResponse().getOutputString() + "] [Error Message=" 
                + trip.getResponse().getErrorMessage() + "]");
    }

}
