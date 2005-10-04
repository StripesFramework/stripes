package net.sourceforge.stripes.examples.ajax;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationError;

import java.io.StringReader;
import java.util.List;

/**
 * A very simple calculator action that is designed to work with an ajax front end.
 * @author Tim Fennell
 */
@UrlBinding("/ajax/Calculator.action")
public class CalculatorActionBean implements ActionBean, ValidationErrorHandler {
    private ActionBeanContext context;
    private double numberOne;
    private double numberTwo;
    public ActionBeanContext getContext() { return context; }

    public void setContext(ActionBeanContext context) { this.context = context; }

    /** Converts errors to HTML and streams them back to the browser. */
    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        StringBuilder message = new StringBuilder();

        for (List<ValidationError> fieldErrors : errors.values()) {
            for (ValidationError error : fieldErrors) {
                message.append("<div class=\"error\">");
                message.append(error.getMessage(getContext().getLocale()));
                message.append("</div>");
            }
        }

        return new StreamingResolution("text/html", new StringReader(message.toString()));
    }

    @Validate(required=true)
    public double getNumberOne() { return numberOne; }
    public void setNumberOne(double numberOne) { this.numberOne = numberOne; }

    @Validate(required=true)
    public double getNumberTwo() { return numberTwo; }
    public void setNumberTwo(double numberTwo) { this.numberTwo = numberTwo; }

    @HandlesEvent("Addition") @DefaultHandler
    public Resolution addNumbers() {
        String result = String.valueOf(numberOne + numberTwo);
        return new StreamingResolution("text", new StringReader(result));
    }

    @HandlesEvent("Division")
    public Resolution divideNumbers() {
        String result = String.valueOf(numberOne / numberTwo);
        return new StreamingResolution("text", new StringReader(result));
    }
}
