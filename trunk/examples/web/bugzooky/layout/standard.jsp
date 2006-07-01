<%@ include file="/bugzooky/taglibs.jsp" %>

<stripes:layout-definition>
    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    <html>
        <head>
            <title>Bugzooky - ${title}</title>
            <link rel="stylesheet"
                  type="text/css"
                  href="${pageContext.request.contextPath}/bugzooky/bugzooky.css"/>
            <script type="text/javascript"
                    src="${pageContext.request.contextPath}/bugzooky/bugzooky.js"></script>
            <stripes:layout-component name="html-head"/>
        </head>
        <body>
            <div id="contentPanel">
                <stripes:layout-component name="header">
                    <jsp:include page="/bugzooky/layout/header.jsp"/>
                </stripes:layout-component>

                <div id="pageContent">
                    <div class="sectionTitle">${title}</div>
                    <stripes:messages/>
                    <stripes:layout-component name="contents"/>
                </div>

                <div id="footer">
                    <stripes:link href="/examples/bugzooky/ViewResource.action">
                        View this JSP
                        <stripes:link-param name="resource" value="${pageContext.request.servletPath}"/>
                    </stripes:link>

                    | View other source files:
                    <stripes:useActionBean binding="/examples/bugzooky/ViewResource.action" var="bean"/>
                    <select style="width: 350px;" onchange="document.location = this.value;">
                        <c:forEach items="${bean.availableResources}" var="file">
                            <stripes:url value="/examples/bugzooky/ViewResource.action" var="url">
                                <stripes:param name="resource" value="${file}"/>
                            </stripes:url>
                            <option value="${url}">${file}</option>
                        </c:forEach>
                    </select>
                    | Built on <a href="http://stripes.mc4j.org">Stripes</a>
                </div>
            </div>
        </body>
    </html>
</stripes:layout-definition>