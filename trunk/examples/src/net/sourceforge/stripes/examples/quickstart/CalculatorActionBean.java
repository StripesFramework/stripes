package net.sourceforge.stripes.examples.quickstart;

import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.validation.Validate;

/**
 * A very simple calculator action.
 * @author Tim Fennell
 */
@UrlBinding("/quickstart/Calculator.action")
public class CalculatorActionBean implements ActionBean {
    private ActionBeanContext context;
    private double numberOne;
    private double numberTwo;
    private double result;

    public ActionBeanContext getContext() { return context; }

    public void setContext(ActionBeanContext context) { this.context = context; }

    @Validate(required=true)
    public double getNumberOne() { return numberOne; }
    public void setNumberOne(double numberOne) { this.numberOne = numberOne; }

    @Validate(required=true, on="Division")
    public double getNumberTwo() { return numberTwo; }
    public void setNumberTwo(double numberTwo) { this.numberTwo = numberTwo; }

    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }

    @HandlesEvent("Addition") @DefaultHandler
    public Resolution addNumbers() {
        result = numberOne + numberTwo;
        return new ForwardResolution("/quickstart/index.jsp");
    }

    @HandlesEvent("Division")
    public Resolution divideNumbers() {
        result = numberOne / numberTwo;
        return new ForwardResolution("/quickstart/index.jsp"); 
    }
}
