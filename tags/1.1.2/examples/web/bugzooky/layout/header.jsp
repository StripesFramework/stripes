<%@ include file="/bugzooky/taglibs.jsp" %>

<div id="imageHeader">
    <table style="padding: 5px; margin: 0px; width: 100%;">
        <tr>
            <td id="pageHeader">bugzooky: stripes demo application</td>
            <td id="loginInfo">
                <c:if test="${not empty user}">
                    Welcome: ${user.firstName} ${user.lastName}
                    |
                    <stripes:link href="/bugzooky/Logout.action">Logout</stripes:link>
                </c:if>
            </td>
        </tr>
    </table>
    <div id="navLinks">
        <stripes:link href="/bugzooky/BugList.jsp">Bug List</stripes:link>
        <stripes:link href="/bugzooky/AddEditBug.jsp">Add Bug</stripes:link>
        <stripes:link href="/bugzooky/BulkAddEditBugs.jsp">Bulk Add</stripes:link>
        <stripes:link href="/bugzooky/AdministerBugzooky.jsp">Administer</stripes:link>
    </div>
</div>