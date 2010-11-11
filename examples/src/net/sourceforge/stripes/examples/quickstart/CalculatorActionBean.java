package net.sourceforge.stripes.examples.quickstart;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.examples.bugzooky.ext.Public;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * A very simple calculator action.
 * @author Tim Fennell
 */
@Public
public class CalculatorActionBean implements ActionBean {
    private ActionBeanContext context;
    @Validate(required=true) private double numberOne;
    @Validate(required=true) private double numberTwo;
    private double result;

    public ActionBeanContext getContext() { return context; }
    public void setContext(ActionBeanContext context) { this.context = context; }

    public double getNumberOne() { return numberOne; }
    public void setNumberOne(double numberOne) { this.numberOne = numberOne; }

    public double getNumberTwo() { return numberTwo; }
    public void setNumberTwo(double numberTwo) { this.numberTwo = numberTwo; }

    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }

    /** An event handler method that adds number one to number two. */
    @DefaultHandler
    public Resolution addition() {
        result = numberOne + numberTwo;
        return new ForwardResolution("/quickstart/index.jsp");
    }

    /** An event handler method that divides number one by number two. */
    public Resolution division() {
        result = numberOne / numberTwo;
        return new ForwardResolution("/quickstart/index.jsp");
    }

    /**
     * An example of a custom validation that checks that division operations
     * are not dividing by zero.
     */
    @ValidationMethod(on="division")
    public void avoidDivideByZero(ValidationErrors errors) {
        if (this.numberTwo == 0) {
            errors.add("numberTwo", new SimpleError("Dividing by zero is not allowed."));
        }
    }
}
