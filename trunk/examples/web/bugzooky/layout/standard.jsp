<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

wheeeeeeee whaaaaaaaaaa whoooooooooaaaaaao
<stripes:layout-definition>
    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    <html>
        <head>
            <title>Bugzooky - ${title}</title>
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
            <stripes:layout-component name="html-head"/>
        </head>
        <body>
            <stripes:layout-component name="header">
                <jsp:include page="/bugzooky/layout/header.jsp"/>
            </stripes:layout-component>

            <div class="pageContent">
                <div class="sectionTitle">${title}</div>
                <stripes:layout-component name="contents"/>
            </div>
        </body>
    </html>
</stripes:layout-definition>
wheeeeeeee whaaaaaaaaaa whoooooooooaaaaaao