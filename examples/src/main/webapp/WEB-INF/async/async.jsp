<%@ page import="net.sourceforge.stripes.examples.async.AsyncActionBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<html>
<head>
    <title>Stripes Async demo</title>
    <script src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
</head>
<body>
<h1>
    Hi, this is an async demo.
</h1>
<s:link beanclass="<%=AsyncActionBean.class%>" event="asyncEvent">
    I'm an async event
</s:link>
<pre>${actionBean.ghResponse}</pre>
</body>
</html>
