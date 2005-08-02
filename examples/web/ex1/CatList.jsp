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

      <table>
          <tr>
              <th>Cat Name</th>
              <th>Color</th>
              <th>Age</th>
              <th>Breed</th>
              <th>Favorite Foods</th>
              <th>Activities</th>
              <th>&nbsp;</th>
          </tr>

      <%
          for (Cat cat : new CatController().getCats()) {
              pageContext.setAttribute("cat", cat);
      %>
              <tr>
                  <td>
                      <%=cat.getName()%><br/>
                  </td>
                  <td><%=cat.getColor()%></td>
                  <td><%=cat.getAge()%></td>
                  <td><%=cat.getBreed().getName()%></td>
                  <td>
                      <%
                          String[] foods = cat.getFavoriteFoods();
                          if (foods != null) {
                              for (int i=0; i<foods.length; ++i) {
                      %>
                                <%=foods[i]%>
                      <%
                              }
                          }
                      %>
                  </td>
                  <td>
                      <%
                          String[] activities = cat.getActivities();
                          if (activities != null) {
                              for (int i=0; i<activities.length; ++i) {
                      %>
                                <%=activities[i]%>
                      <%
                              }
                          }
                      %>
                  </td>
                  <td>
                    <stripes:form name="ex1/CatDetailsForm" action="/action/CatDetails" method="post">
                        <stripes:hidden name="cat.name" value="${cat.name}"/>
                        <stripes:hidden name="cat.activities" value="${cat.activities}"/>
                        <stripes:submit name="Edit" value="Edit Kitty">Really, Edit Kitty</stripes:submit>
                    </stripes:form>
                  </td>
              </tr>
      <%
          }
      %>

      </table>

      <stripes:form name="ex1/CatDetailsForm" action="/action/CatDetails">
          <input type="submit" name="New" value="Add Kitty"/>
      </stripes:form>
  </body>
</html>
