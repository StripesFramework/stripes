<%@ page import="net.sourceforge.stripes.examples.ex1.*"%>
<%@ page import="net.sourceforge.stripes.validation.ValidationError"%>
<%@ page import="net.sourceforge.stripes.validation.ValidationErrors"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://stripes.sourceforge.net/stripes.tld" prefix="stripes" %>

<html>
  <head>
      <title>Stripes Examples: Example 1 - Cats</title>
      <link rel="stylesheet" href="/stripes/css/stripes.css" type="text/css"/>
  </head>
  <body>
      <h1>Example 1 - Cats</h1>

      <%
          CatDetailActionBean bean = (CatDetailActionBean) request.getAttribute("ex1/CatDetailsForm");

          if (bean != null && bean.getContext().getValidationErrors() != null) {
              out.write("<div>Validation Errors:</div>");
              out.write("<ul>");
              ValidationErrors errors = bean.getContext().getValidationErrors();

              Iterator listIterator = errors.values().iterator();
              while (listIterator.hasNext()) {
                  Iterator innerIterator = ((List) listIterator.next()).iterator();

                  while (innerIterator.hasNext()) {
                      ValidationError error = (ValidationError) innerIterator.next();
                      out.write("<li>");
                      out.write(error.getMessage(request.getLocale()));
                      out.write("</li>");
                  }
              }

              out.write("</ul>");
          }

      pageContext.setAttribute("activites", Activity.getActivities());
      %>

      <stripes:form method="post" action="/dispatcher" name="ex1/CatDetailsForm">
          <table>
              <tr>
                  <td>Cat Name:</td>
                  <td><stripes:text name="cat.name" class="text"/></td>
              </tr>
              <tr>
                  <td>Color:</td>
                  <td><stripes:text name="cat.color" class="text"/></td>
              </tr>
              <tr>
                  <td>Age:</td>
                  <td><stripes:text name="cat.age" class="text"/></td>
              </tr>
              <tr>
                  <td>Breed:</td>
                  <td>
                      <stripes:radio name="cat.breed" value="<%=Breed.RUSSIAN_BLUE.name()%>"/> Russian Blue |
                      <stripes:radio name="cat.breed" value="<%=Breed.MIX.name()%>"/> Mix |
                      <stripes:radio name="cat.breed" value="<%=Breed.DSH.name()%>"/> DSH |
                      <stripes:radio name="cat.breed" value="<%=Breed.BRITISH_BLUE.name()%>"/> British Blue
                  </td>
              </tr>
              <tr>
                  <td>Favorite Foods:</td>
                  <td>
                      <stripes:checkbox name="cat.favoriteFoods" value="Tuna"/> Tuna |
                      <stripes:checkbox name="cat.favoriteFoods" value="Salmon"/> Salmon |
                      <stripes:checkbox name="cat.favoriteFoods" value="Cardboard"/> Cardboard
                  </td>
              </tr>
              <tr>
                  <td>Activities:</td>
                  <td>
                      <stripes:select name="cat.activities" multiple="multiple">
                          <stripes:options-collection collection="${activites}"
                                                      value="name"
                                                      label="description"/>
                      </stripes:select>
                  </td>
              </tr>
              <tr>
                  <td>Biography:</td>
                  <td>
                      <stripes:file name="biography"/>
                      <br/>
                      Or
                      <br/>
                      <stripes:textarea name="inlineBiography"/>
                  </td>
              </tr>
              <tr>
                  <td colspan="2">
                      <stripes:submit name="Update" value="Update Kitty!"/>
                  </td>
              </tr>
          </table>
    </stripes:form>
  </body>
</html>
