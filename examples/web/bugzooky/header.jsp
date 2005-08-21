<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
    <head>
        <title>Bugzooky - Stripes Examples</title>
        <link rel="stylesheet"
              type="text/css"
              href="${pageContext.request.contextPath}/bugzooky/bugzooky.css"/>
        <script type="text/javascript">
            /**
             * JS Object used to store the last clicked checkbox for each set of
             * checkboxes. See method below for usage details.
             */
            var lastClickedIndexes = new Object();

            /**
             * If called by a checkbox when the checkbox is clicked, will manage the
             * selection/deselection of a range of checkboxes when the Shift key
             * is held down during the click.
             *
             * @param clickedCheckbox - the checkbox originating the event
             * @param event - the onclick event itself
             */
            function handleCheckboxRangeSelection(clickedCheckbox, event) {
                // Find the position of the checkbox in the array of checkboxes
                checkBoxes = document.getElementsByName(clickedCheckbox.name);
                var clickedIndex;

                for (i=0; i<checkBoxes.length; i++) {
                    if (clickedCheckbox.value == checkBoxes[i].value) {
                        clickedIndex = i;
                        break;
                    }
                }

                // Fetch the index of the last clicked checkbox for this set of checkboxes
                lastClickedIndex = lastClickedIndexes[clickedCheckbox.name];

                // If the shift key was pressed, and we have a previously clicked
                // checkbox, "click" the whole range
                if ((event.shiftKey == true) && (lastClickedIndex != null))  {
                    startId = Math.min(lastClickedIndex, clickedIndex);
                    endId   = Math.max(lastClickedIndex, clickedIndex);

                    for (i=startId; i<=endId; i++) {
                        checkBoxes[i].checked = clickedCheckbox.checked;
                    }
                }

                // Store the new checkbox index as the last clicked one
                lastClickedIndexes[clickedCheckbox.name] = clickedIndex;
            }
        </script>
    </head>
  <body>
      <div class="pageheader">Bugzooky - Stripes Example Application</div>
      <ul class="topnav">
          <li><stripes:link href="/bugzooky/BugList.jsp">Bug List</stripes:link></li>
          <li><stripes:link href="/bugzooky/AddEditBug.jsp">Add Bug</stripes:link></li>
          <li><stripes:link href="/bugzooky/BulkAddEditBugs.jsp">Bulk Add</stripes:link></li>
          <li><stripes:link href="/bugzooky/AdministerBugzooky.jsp">Administer</stripes:link></li>
      </ul>

      <div class="pageContent">