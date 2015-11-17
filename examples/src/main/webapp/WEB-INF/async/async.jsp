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
</body>
</html>
