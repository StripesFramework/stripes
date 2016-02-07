<%@ page import="net.sourceforge.stripes.examples.async.AsyncActionBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Stripes Async demo</title>
    <script src="${pageContext.request.contextPath}/rockandroll/js/libs/jquery-1.9.1.js"></script>
</head>
<body>
<h1>
    Hi, this is an async demo.
</h1>
<s:link beanclass="<%=AsyncActionBean.class%>" event="asyncEvent">
    <s:param name="someProp" value="foobar"/>
    I'm an async event
</s:link>
<br/>
<s:link beanclass="<%=AsyncActionBean.class%>" event="asyncEventThatTimeouts">
    I'm an async event that timeouts
</s:link>
<br/>
<s:link beanclass="<%=AsyncActionBean.class%>" event="asyncEventThatThrows">
    I'm an async event that throws an Exception
</s:link>
<hr/>
<s:errors/>
<s:form beanclass="<%=AsyncActionBean.class%>">
    <s:text name="someProp"/>
    <s:submit name="asyncEvent"/>
</s:form>
<c:if test="${not empty actionBean.ghResponse}">
    <hr/>
    <div id="result"></div>
    <script type="application/json" id="gh-response">${actionBean.ghResponse}</script>
    <script type="text/javascript">
        $(function() {
            var commits = JSON.parse($('#gh-response').text());
            var nbCommits = commits.length;
            var html = "<p>Displaying " + nbCommits + " commits received asynchronously from GitHub !</p><ul>";
            $.each(commits, function(index, commit) {
                var author = commit.commit.author.name;
                var msg = commit.commit.message;
                html +=  "<li>" + author + " : " + msg + "</li>";
            });
            html += "</ul>";
            $("#result").html(html);
        });
    </script>
</c:if>
<hr/>
<a href="#" id="asyncWrites">
    I trigger async writes to the response
</a>
<div id="asyncResponses"></div>
<script type="text/javascript">
    $('#asyncWrites').click(function(e) {
        e.preventDefault();
        var asyncResponses = $('#asyncResponses');
        asyncResponses.empty();
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function(){
            console.log("readyState=" + xhr.readyState);
            if((xhr.readyState == 3 || xhr.readyState == 4) && xhr.status == 200){
                console.log("responseText=" + xhr.responseText);
                asyncResponses.html(xhr.responseText);
            }
        };
        xhr.open("GET", "${pageContext.request.contextPath}/async?asyncWrites=true", true);
        xhr.send();
    })
</script>
</body>
</html>
