<%@ include file="/bugzooky/taglibs.jsp"%>

<stripes:layout-render name="/bugzooky/layout/standard.jsp" title="Administer Bugzooky">
	<stripes:layout-component name="contents">

		<div class="subtitle">People</div>
		<stripes:useActionBean var="peopleActionBean"
			beanclass="net.sourceforge.stripes.examples.bugzooky.AdministerPeopleActionBean" />
		<stripes:form beanclass="net.sourceforge.stripes.examples.bugzooky.AdministerPeopleActionBean">
			<stripes:errors />

			<table class="display">
				<tr>
					<th><img src="${ctx}/bugzooky/trash.png" title="Delete" /></th>
					<th>ID</th>
					<th>Username</th>
					<th>First Name</th>
					<th>Last Name</th>
					<th>Email</th>
				</tr>
				<c:forEach items="${peopleActionBean.people}" var="person" varStatus="loop">
					<tr>
						<td><stripes:checkbox name="deleteIds" value="${person}" onclick="handleCheckboxRangeSelection(this, event);" /></td>
						<td>
							${person.id}
							<stripes:hidden name="people[${loop.index}]" />
						</td>
						<td><stripes:text name="people[${loop.index}].username" /></td>
						<td><stripes:text name="people[${loop.index}].firstName" /></td>
						<td><stripes:text name="people[${loop.index}].lastName" /></td>
						<td><stripes:text name="people[${loop.index}].email" /></td>
					</tr>
					<c:set var="newIndex" value="${loop.index + 1}" scope="page" />
				</c:forEach>
				<%-- And now, an empty row, to allow the adding of new users. --%>
				<tr>
					<td></td>
					<td></td>
					<td><stripes:text name="people[${newIndex}].username" /></td>
					<td><stripes:text name="people[${newIndex}].firstName" /></td>
					<td><stripes:text name="people[${newIndex}].lastName" /></td>
					<td><stripes:text name="people[${newIndex}].email" /></td>
				</tr>
			</table>

			<div class="buttons"><stripes:submit name="Save" value="Save Changes" /></div>
		</stripes:form>

		<div class="subtitle">Components</div>
		<stripes:useActionBean var="componentsActionBean"
			beanclass="net.sourceforge.stripes.examples.bugzooky.AdministerComponentsActionBean" />
		<stripes:form beanclass="net.sourceforge.stripes.examples.bugzooky.AdministerComponentsActionBean">
			<stripes:errors />

			<table class="display" style="width: auto;">
				<tr>
					<th><img src="${ctx}/bugzooky/trash.png" title="Delete" /></th>
					<th>ID</th>
					<th>Component Name</th>
				</tr>
				<c:forEach items="${componentsActionBean.components}" var="component" varStatus="loop">
					<tr>
						<td><stripes:checkbox name="deleteIds" value="${component}"
							onclick="handleCheckboxRangeSelection(this, event);" /></td>
						<td>
							${component.id}
							<stripes:hidden name="components[${loop.index}]" />
						</td>
						<td><stripes:text name="components[${loop.index}].name" /></td>
					</tr>
					<c:set var="newIndex" value="${loop.index + 1}" scope="page" />
				</c:forEach>
				<%-- And now, an empty row, to allow adding new components. --%>
				<tr>
					<td></td>
					<td></td>
					<td><stripes:text name="components[${newIndex}].name" /></td>
				</tr>
			</table>

			<div class="buttons"><stripes:submit name="save" value="Save Changes" /></div>
		</stripes:form>
	</stripes:layout-component>
</stripes:layout-render>