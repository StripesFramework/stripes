<%@ include file="/bugzooky/taglibs.jsp" %>

<div id="imageHeader">
    <table style="padding: 5px; margin: 0px; width: 100%;">
        <tr>
            <td id="pageHeader">bugzooky: stripes demo application</td>
            <td id="loginInfo">
                <c:if test="${not empty user}">
                    Welcome: <stripes:format value="${user}" formatType="full" formatPattern="%F %L" />
                    |
                    <stripes:link beanclass="net.sourceforge.stripes.examples.bugzooky.LogoutActionBean">Logout</stripes:link>
                </c:if>
            </td>
        </tr>
    </table>
    <div id="navLinks">
        <stripes:link beanclass="net.sourceforge.stripes.examples.bugzooky.BugListActionBean">Bug List</stripes:link>
        <stripes:link beanclass="net.sourceforge.stripes.examples.bugzooky.SingleBugActionBean">Add Bug</stripes:link>
        <stripes:link beanclass="net.sourceforge.stripes.examples.bugzooky.MultiBugActionBean">Bulk Add</stripes:link>
        <stripes:link beanclass="net.sourceforge.stripes.examples.bugzooky.AdministerComponentsActionBean">Administer</stripes:link>
    </div>
</div>