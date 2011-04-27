<%@ include file="/bugzooky/taglibs.jsp"%>

<stripes:layout-render name="/bugzooky/layout/standard.jsp" title="Login">
	<stripes:layout-component name="contents">

		<table style="vertical-align: top;">
			<tr>
				<td style="width: 25%; vertical-align: top;">
				<!-- Somewhat contrived example of using the errors tag 'beanclass' attribute. -->
				<stripes:errors beanclass="${actionBean.class}" />
				<stripes:form beanclass="${actionBean.class}" focus="username">
					<table>
						<tr>
							<td style="font-weight: bold;"><stripes:label for="username" />:</td>
						</tr>
						<tr>
							<td><stripes:text name="username" value="${user.username}" /></td>
						</tr>
						<tr>
							<td style="font-weight: bold;"><stripes:label for="password" />:</td>
						</tr>
						<tr>
							<td><stripes:password name="password" /></td>
						</tr>
						<tr>
							<td style="text-align: center;">
							<%-- If the security interceptor attached a targetUrl, carry that along. --%>
							<stripes:hidden name="targetUrl" />
							<stripes:submit name="login" value="Login" />
							</td>
						</tr>
					</table>
				</stripes:form></td>
				<td style="vertical-align: top;"><c:choose>
					<c:when test="${empty user}">
						<div class="sectionTitle">Welcome</div>

						<p>Welcome to Bugzooky, the Stripes demo application. If you haven't already created an account, you will need
						to <stripes:link beanclass="net.sourceforge.stripes.examples.bugzooky.RegisterActionBean">register</stripes:link>
						in order to log in.</p>

						<div class="sectionTitle">Intuitive development</div>

						<p><a href="http://stripesframework.org">Stripes</a> is a relatively new framework for developing web
						applications in Java. It uses an action-oriented model that will be familiar to anyone who has previously used
						Struts or anything similar. Stripes is designed to be extremely easy to develop with - to be intuitive to
						developers and to require the minimum amount of code and configuration.</p>

						<p>As you try out the demo application you can use the links in the footer to view the source of the current
						JSP, and of other files that are part of the application.</p>
					</c:when>

					<c:otherwise>
						<p>You are already logged in as '${user.username}'. Logging in again will cause you to be logged out, and then
						re-logged in with the username and password supplied.</p>
					</c:otherwise>
				</c:choose></td>
			</tr>
		</table>

	</stripes:layout-component>
</stripes:layout-render>
