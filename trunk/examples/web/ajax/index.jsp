<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
      <title>My First Ajax Stripe</title>
      <script type="text/javascript"
              src="${pageContext.request.contextPath}/ajax/prototype.js"></script>
      <script type="text/javascript" xml:space="preserve">
          /*
           * Function that uses Prototype to invoke an action of a form. Slurps the values
           * from the form using prototype's 'Form.serialize()' method, and then submits
           * them to the server using prototype's 'Ajax.Updater' which transmits the request
           * and then renders the resposne text into the named container.
           *
           * @param form reference to the form object being submitted
           * @param event the name of the event to be triggered, or null
           * @param container the name of the HTML container to insert the result into
           */
          function invoke(form, event, container) {
              var params = Form.serialize(form);
              if (event != null) params = event + '&' + params;
              new Ajax.Updater(container, form.action, {method:'post', postBody:params});
          }
      </script>
  </head>
  <body>
    <h1>Stripes Ajax Calculator</h1>

    <p>Hi, I'm the Stripes Calculator. I can only do addition. Maybe, some day, a nice programmer
    will come along and teach me how to do other things?</p>

    <stripes:form action="/examples/ajax/Calculator.action">
        <table>
            <tr>
                <td>Number 1:</td>
                <td><stripes:text name="numberOne"/></td>
            </tr>
            <tr>
                <td>Number 2:</td>
                <td><stripes:text name="numberTwo"/></td>
            </tr>
            <tr>
                <td colspan="2">
                    <stripes:button name="add" value="Add"
                        onclick="invoke(this.form, this.name, 'result');"/>
                    <stripes:button name="divide" value="Divide"
                        onclick="invoke(this.form, this.name, 'result');"/>
                </td>
            </tr>
            <tr>
                <td>Result:</td>
                <td id="result"></td>
            </tr>
        </table>
    </stripes:form>
  </body>
</html>