<%@ include file="/bugzooky/taglibs.jsp" %>

<stripes:layout-render name="/bugzooky/layout/standard.jsp" title="Bug List">
    <stripes:layout-component name="contents">

        <jsp:useBean id="bugManager" scope="page"
                     class="net.sourceforge.stripes.examples.bugzooky.biz.BugManager"/>

        <stripes:form action="/examples/bugzooky/MultiBug.action">
            <stripes:errors/>

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
                        <td><stripes:checkbox name="bugIds" value="${bug.id}"
                                              onclick="handleCheckboxRangeSelection(this, event);"/></td>
                        <td>${bug.id}</td>
                        <td><fmt:formatDate value="${bug.openDate}" dateStyle="medium"/></td>
                        <td>${bug.shortDescription}</td>
                        <td>${bug.component.name}</td>
                        <td>${bug.priority}</td>
                        <td>${bug.status}</td>
                        <td>${bug.owner.username}</td>
                        <td>
                            <stripes:link href="/examples/bugzooky/SingleBug.action" event="preEdit">
                                Edit
                                <stripes:link-param name="bug.id" value="${bug.id}"/>
                            </stripes:link>
                        </td>
                    </tr>
                </c:forEach>
            </table>

            <div class="buttons"><stripes:submit name="preEdit" value="Bulk Edit"/></div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>