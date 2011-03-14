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
