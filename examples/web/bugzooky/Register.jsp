<%@ page import="net.sourceforge.stripes.examples.bugzooky.biz.Status"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/bugzooky/taglibs.jsp" %>

<stripes:layout-render name="/bugzooky/layout/standard.jsp" title="Register">
    <stripes:layout-component name="contents">

        <stripes:errors globalErrorsOnly="true"/>

        <stripes:form action="/examples/bugzooky/Register.action" focus="first">
            <p>Please provide the following information:</p>

            <table class="leftRightForm">
                <tr>
                    <th><stripes:label for="user.firstName"/>:</th>
                    <td>
                        <stripes:text name="user.firstName"/>
                        <stripes:errors field="user.firstName"/>
                    </td>
                </tr>
                <tr>
                    <th><stripes:label for="user.lastName"/>:</th>
                    <td>
                        <stripes:text name="user.lastName"/>
                        <stripes:errors field="user.lastName"/>
                    </td>
                </tr>
                <tr>
                    <th><stripes:label for="user.username"/>:</th>
                    <td>
                        <stripes:text name="user.username"/>
                        <stripes:errors field="user.username"/>
                    </td>
                </tr>
            </table>

            <div class="buttons">
                <stripes:submit name="gotoStep2" value="Next"/>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>