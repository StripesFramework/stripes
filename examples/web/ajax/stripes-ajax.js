///////////////////////////////////////////////////////////////////////////////
// AJAX with Stripes sample JavaScript library. Provides functions to use an
// XMLHttpRequest to invoke a Stripes action (or other server side component)
// using the contents of a HTML form or a JavaScript object as parameters.
//
// Author: Tim Fennell
// (C) Copyright Tim Fennell, 2005.
///////////////////////////////////////////////////////////////////////////////

/**
 * Invokes a Stripes action using the contents of the supplied form. The named
 * event will be invoked on the server, and the callbackFunction will be
 * invoked. The callback function will only be invoked when the request is
 * completed, and (unlike when using the XMLHttpRequest directly) the
 * XMLHttpRequest will be supplied as the first parameter to the callback
 * function.
 *
 * @param form - a reference to a form object to be "submitted" using AJAX
 * @param event - the String name of the event to be fired when invoking the
 *                ActionBean. To invoke the default event pass either null or ""
 * @param callbackFunction - a function that takes a single parameter, the
 *        XMLHttpRequest, and will be invoked when the request has been processed
 * @return a reference to the XMLHttpRequest in case the caller would like to be
 *         able to manipulate it between sending the request and receive the
 *         callback (e.g. cancelling it before re-invoking).
 */
function invokeActionForm(form, event, callbackFunction) {
    var url = form.action;
    if (event) {
        url += "?" + event + "=" +  getFormValuesAsQueryString(form);
    }
    else {
        url += "?" + getFormValuesAsQueryString(form).slice(1);
    }

    return fetchFromUrl(url, callbackFunction);
}


/**
 * Invokes a Stripes action bound to the specified URL. May specify a named
 * event, or if an event name is not supplied, will invoke the default event
 * handler.
 *
 * @param url - the URL for the http request, which will be accessed asynchronously
 * @param event - the String name of the event to be fired when invoking the
 *                ActionBean. To invoke the default event pass either null or ""
 * @param params - a JavaScript object. Each property on the object will be
 *        in the query string sent to the server. If a property is an array each
 *        value will be included, otherwise the property will be treated as a
 *        single value.
 * @param callbackFunction - a function that takes a single parameter, the
 *        XMLHttpRequest, and will be invoked when the request has been processed
 * @return a reference to the XMLHttpRequest in case the caller would like to be
 *         able to manipulate it between sending the request and receive the
 *         callback (e.g. cancelling it before re-invoking).
 */
function invokeActionUrl(url, event, params, callbackFunction) {
    var composedUrl = url;
    var linkChar = "?";

    if (event) {
        composedUrl += linkChar + event + "=";
        linkChar = "&";
    }

    if (params) {
        for (var key in params) {
            value = params[key];
            if (value instanceof Array) {
                for (var i=0; i<value.length; i++) {
                    composedUrl += linkChar + key + "=" + escape(value[i]);
                    linkChar = "&";
                }
            }
            else {
                composedUrl += linkChar + key + "=" + escape(value);
                linkChar = "&";
            }
        }
    }

    return fetchFromUrl(composedUrl, callbackFunction);
}


/**
 * Takes a form object and returns a JavaScript object containing the form field
 * names and values as would be submitted to the server.  Correctly handles
 * radio, checkboxes, single and multi selects - including them in the request
 * only if one or more values has been specified. Excludes button types
 * (submit, reset, button)
 *
 * @return a JavaScript object with a property for each form field to be
 *         submitted. The value of each property is an Array containing one or
 *         more values to be submitted.
 */
function getFormValuesAsMap(form) {
	var params = new Object();
	var inputs = form.elements;
	for (var i=0; i<inputs.length; i++) {
		var input = inputs[i];

		if (input.type == "radio" || input.type == "checkbox") {
			// For radios and checkboxes, include them only if checked
			if (input.checked == true) {
                if (params[input.name] == null) params[input.name] = new Array();
                params[input.name].push(input.value);
			}
		}
		else if (input.type == "select-multiple") {
			// For multi-selects we have to check each value
			for (var j=0; j<input.options.length; j++) {
				if (input.options[j].selected == true) {
                    if (params[input.name] == null) params[input.name] = new Array();
                    params[input.name].push(input.options[j].value);
				}
			}
		}
		else if (input.type == "submit" || input.type == "button" || input.type == "reset") {
			// Don't include button types in the submit
		}
		else {
			// Include any other type as a straight name=value
            if (params[input.name] == null) params[input.name] = new Array();
            params[input.name].push(input.value);
		}
	}

    return params;
}

/**
 * Takes a form object and returns a query string containing all the fields
 * that would be submitted to the server. See getFormValuesAsMap(form) for more
 * details on what's included. Each value is escaped before being included in
 * the query string.
 *
 * @return a query string. The query string will always start with an ampersand.
 */
function getFormValuesAsQueryString(form) {
    var params = getFormValuesAsMap(form);
    var queryString = "";

    for (var name in params) {
        var values = params[name];
        for (var i=0; i<values.length; i++) {
            queryString += "&" + name + "=" + escape(values[i]);
        }
    }

    return queryString;
}

/**
 * Gets a new XMLHttpRequest object.
 */
function getXmlHttpRequest() {
    try { return new XMLHttpRequest(); }
    catch (ex) { try {  return new ActiveXObject('Msxml2.XMLHTTP'); }
        catch (ex1) { try { return new ActiveXObject('Microsoft.XMLHTTP'); }
            catch(ex1) {       return new ActiveXObject('Msxml2.XMLHTTP.4.0'); }
        }
    }
}

/**
 * Utility method to make a request to the supplied URL and invoked hte callback
 * function, passing it the XMLHttpRequest.
 */
function fetchFromUrl(url, callbackFunction) {
    var xmlHttpRequest = getXmlHttpRequest();
    xmlHttpRequest.onreadystatechange = function() {
        if (xmlHttpRequest.readyState == 4 && callbackFunction != null) {
            callbackFunction(xmlHttpRequest);
        }
    }

    xmlHttpRequest.open("GET", url, true, "", "");
    xmlHttpRequest.send("");
    return xmlHttpRequest;
}
