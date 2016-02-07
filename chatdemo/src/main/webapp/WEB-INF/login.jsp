<%@ page import="net.sourceforge.stripes.chatdemo.actions.LoginActionBean" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="/WEB-INF/layout.jsp" title="Login">
    <stripes:layout-component name="body">

        <h1>Pick a username and enter the chat</h1>

        <stripes:errors/>

        <stripes:form beanclass="<%=LoginActionBean.class%>">
            <stripes:text name="username"/>
            <stripes:submit name="login"/>
        </stripes:form>

    </stripes:layout-component>
</stripes:layout-render>