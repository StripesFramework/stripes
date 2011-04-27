<%-- This JSP demonstrates a common technique for making an ActionBean your default resource --%>
<%@ include file="taglibs.jsp"%>
<stripes:url var="url" beanclass="net.sourceforge.stripes.examples.bugzooky.LoginActionBean" prependContext="false" />
<jsp:forward page="${url}" />