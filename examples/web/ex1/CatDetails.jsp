<%@ page import="net.sourceforge.stripes.examples.ex1.*"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://stripes.sourceforge.net/stripes.tld" prefix="stripes" %>

<html>
  <head>
      <title>Stripes Examples: Example 1 - Cats</title>
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/stripes.css" type="text/css"/>
  </head>
  <body>
      <h1>Example 1 - Cats</h1>

      <%
      pageContext.setAttribute("activites", Activity.getActivities());
      %>

      <%-- Output the errors block if there are errors for the form. --%>
      <stripes:errors>
          <stripes:errors-header>
              <div class="errorHeader">Alert!  Errors!</div>
              <ul type="square">
          </stripes:errors-header>

          <li>Error #${index}: <stripes:individual-error/></li>

          <stripes:errors-footer></ul></stripes:errors-footer>
      </stripes:errors>

      <stripes:form method="post" action="/action/CatDetails" name="ex1/CatDetailsForm">
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
                  <td>Date of Birth:</td>
                  <td><stripes:text name="cat.dateOfBirth" class="text" formatType="date" formatPattern="medium"/></td>
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
                      <stripes:reset name="resetButton"/>
                  </td>
              </tr>
          </table>
    </stripes:form>
  </body>
</html>
