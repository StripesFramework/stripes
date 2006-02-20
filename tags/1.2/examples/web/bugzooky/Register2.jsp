<%@ page import="net.sourceforge.stripes.examples.bugzooky.biz.Status"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/bugzooky/taglibs.jsp" %>

<stripes:layout-render name="/bugzooky/layout/standard.jsp" title="Register">
    <stripes:layout-component name="contents">

        <stripes:form action="/bugzooky/Register.action" method="POST">
            <stripes:errors/>

            <p>Welcome ${actionBean.user.firstName}, please pick a password:</p>

            <table class="leftRightForm">
                <tr>
                    <th><stripes:label for="user.password"/>:</th>
                    <td><stripes:password name="user.password"/></td>
                </tr>
                <tr>
                    <th><stripes:label for="confirmPassword"/>:</th>
                    <td><stripes:password name="confirmPassword"/></td>
                </tr>
            </table>

            <div class="buttons">
                <stripes:submit name="Register" value="Create Account"/>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>