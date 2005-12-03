<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
      <title>My First Ajax Stripe</title>
      <script type="text/javascript" xml:space="preserve">
          function populateResult(xmlHttpRequest) {
              var td = document.getElementById("result");
              td.innerHTML = xmlHttpRequest.responseText;
          }
      </script>
      <script type="text/javascript"
              src="${pageContext.request.contextPath}/ajax/stripes-ajax.js"></script>
  </head>
  <body>
    <h1>Stripes Ajax Calculator</h1>

    <p>Hi, I'm the Stripes Calculator. I can only do addition. Maybe, some day, a nice programmer
    will come along and teach me how to do other things?</p>

    <stripes:form action="/ajax/Calculator.action">
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
                    <stripes:button name="Addition" value="Add"
                        onclick="invokeActionForm(this.form, this.name, populateResult);"/>
                    <stripes:button name="Division" value="Divide"
                        onclick="invokeActionForm(this.form, this.name, populateResult);"/>
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