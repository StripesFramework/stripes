<%@ page import="net.sourceforge.stripes.examples.ex1.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://stripes.sourceforge.net/stripes.tld" prefix="stripes" %>

<html>
  <head>
      <title>Stripes Examples: Example 1 - Cats</title>
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/stripes.css" type="text/css"/>
      </head>
  <body>
      <h1>Example 1 - Cats</h1>

      <stripes:form action="/action/cat/BulkAdd">
          <table>
              <tr>
                  <th>Cat Name</th>
                  <th>Color</th>
                  <th>Age</th>
                  <th>Breed</th>
              </tr>

          <%
              for (int i=0; i<3; ++i) {
                  pageContext.setAttribute("index", i);
          %>
                  <tr>
                      <td>
                          <stripes:text name="cats[${index}].name"/>
                      </td>
                      <td>
                          <stripes:text name="cats[${index}].color"/>
                      </td>
                      <td>
                          <stripes:text name="cats[${index}].age"/>
                      </td>
                      <td>
                          <stripes:select name="cats[${index}].breed">
                              <stripes:options-enumeration enum="net.sourceforge.stripes.examples.ex1.Breed" label="name"/>
                          </stripes:select>
                      </td>
                  </tr>
          <%
              }
          %>

          </table>

          <stripes:submit name="Add" value="Add Em All"/>
        </stripes:form>
  </body>
</html>
