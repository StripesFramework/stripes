<%@ include file="/bugzooky/taglibs.jsp"%>

<stripes:layout-render name="/bugzooky/layout/standard.jsp" title="Bug List">
	<stripes:layout-component name="contents">

		<jsp:useBean id="bugManager" scope="page" class="net.sourceforge.stripes.examples.bugzooky.biz.BugManager" />

		<stripes:form beanclass="net.sourceforge.stripes.examples.bugzooky.MultiBugActionBean">
			<stripes:errors />

			<table class="display">
				<tr>
					<th></th>
					<th>ID</th>
					<th>Opened On</th>
					<th>Description</th>
					<th>Component</th>
					<th>Priority</th>
					<th>Status</th>
					<th>Owner</th>
					<th></th>
				</tr>
				<c:forEach items="${bugManager.allBugs}" var="bug" varStatus="rowstat">
					<tr class="${rowstat.count mod 2 == 0 ? "even" : "odd"}">
						<td><stripes:checkbox name="bugs" value="${bug}" onclick="handleCheckboxRangeSelection(this, event);" /></td>
						<td>${bug.id}</td>
						<td><fmt:formatDate value="${bug.openDate}" dateStyle="medium" /></td>
						<td>${bug.shortDescription}</td>
						<td>${bug.component.name}</td>
						<td>${bug.priority}</td>
						<td>${bug.status}</td>
						<td>${bug.owner.username}</td>
						<td><stripes:link beanclass="net.sourceforge.stripes.examples.bugzooky.SingleBugActionBean">
                                Edit
                                <stripes:param name="bug" value="${bug}" />
						</stripes:link></td>
					</tr>
				</c:forEach>
			</table>

			<div class="buttons"><stripes:submit name="view" value="Bulk Edit" /></div>
		</stripes:form>
	</stripes:layout-component>
</stripes:layout-render>