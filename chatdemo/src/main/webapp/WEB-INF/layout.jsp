<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<stripes:layout-definition>
    <!DOCTYPE html>
    <html>
    <head>
        <title>${pageTitle}</title>
        <link rel="stylesheet" href="${cp}/style/chat.css"/>
        <stripes:layout-component name="head"/>
    </head>
    <body>
    <stripes:layout-component name="body"/>
    </body>
    </html>
</stripes:layout-definition>