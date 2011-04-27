<%@ include file="/bugzooky/taglibs.jsp" %>

<stripes:layout-definition>
    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    <html>
        <head>
            <title>Bugzooky - ${title}</title>
            <link rel="stylesheet" type="text/css" href="${ctx}/bugzooky/bugzooky.css"/>
            <script type="text/javascript" src="${ctx}/bugzooky/bugzooky.js"></script>
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
                    <stripes:url var="view" beanclass="net.sourceforge.stripes.examples.bugzooky.ViewResourceActionBean" />
                    <stripes:link href="${view}${pageContext.request.servletPath}">
                        View this JSP
                    </stripes:link>

                    | View other source files:
                    <stripes:useActionBean beanclass="net.sourceforge.stripes.examples.bugzooky.ViewResourceActionBean" var="bean"/>
                    <select style="width: 350px;" onchange="document.location = this.value;">
                        <c:forEach items="${bean.availableResources}" var="file">
                            <option value="${view}${file}">${file}</option>
                        </c:forEach>
                    </select>
                    | Built on <a href="http://www.stripesframework.org">Stripes</a>
                </div>
            </div>
        </body>
    </html>
</stripes:layout-definition>
