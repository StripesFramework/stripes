<%@ page import="net.sourceforge.stripes.examples.bugzooky.biz.Status"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/bugzooky/taglibs.jsp" %>

<stripes:layout-render name="/bugzooky/layout/standard.jsp" title="Register">
    <stripes:layout-component name="contents">

        <stripes:form action="/bugzooky/Register.action" method="POST">
            <stripes:errors/>

            <p>Please provide the following information:</p>

            <table class="leftRightForm">
                <tr>
                    <th><stripes:label for="user.firstName"/>:</th>
                    <td><stripes:text name="user.firstName"/></td>
                </tr>
                <tr>
                    <th><stripes:label for="user.lastName"/>:</th>
                    <td><stripes:text name="user.lastName"/></td>
                </tr>
                <tr>
                    <th><stripes:label for="user.username"/>:</th>
                    <td><stripes:text name="user.username"/></td>
                </tr>
            </table>

            <div class="buttons">
                <stripes:submit name="GotoStep2" value="Next"/>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>