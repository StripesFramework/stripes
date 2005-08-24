/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.tag;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.*;

/**
 * This tag library validator verifies that stripes tags are used in the correct hiearchy,
 * for example by checking that the stripes:hidden tag is within a stripes:form tag.
 *
 * @author Greg Hinkle, Tim Fennell
 */
public class TagLibraryValidator extends javax.servlet.jsp.tagext.TagLibraryValidator{

    public ValidationMessage[] validate(String prefix, String uri, PageData pageData) {
        try {
            JSPSaxHandler handler = new JSPSaxHandler();
            SAXParserFactory.newInstance().newSAXParser().parse(pageData.getInputStream(), handler);

            List<ValidationMessage> messages = handler.getValidationMessages();
            if (messages.size() > 0) {
                return messages.toArray(new ValidationMessage[messages.size()]);
            }

        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }


    /**
     * This class is the SAX default handler and tracks the hierarchy of custom
     * tags as it parses the XML version of a JSP.
     */
    public static class JSPSaxHandler extends DefaultHandler {

        Stack<String> stack = new Stack<String>();

        List<ValidationMessage> validationMessages = new  ArrayList<ValidationMessage>();

        /** A map of child tags to their required parents */
        static Map<String,String> requiredParentMap = new HashMap<String, String>();

        /** A static map of child tags to tags they must not be inside of. */
        static Map<String,String> verbotenParentMap = new HashMap<String,String>();

        static {
            // Form fields must be inside forms
            requiredParentMap.put("stripes:text",     "stripes:form");
            requiredParentMap.put("stripes:radio",    "stripes:form");
            requiredParentMap.put("stripes:checkbox", "stripes:form");
            requiredParentMap.put("stripes:select",   "stripes:form");
            requiredParentMap.put("stripes:file",     "stripes:form");
            requiredParentMap.put("stripes:textarea", "stripes:form");
            requiredParentMap.put("stripes:submit",   "stripes:form");
            requiredParentMap.put("stripes:reset",    "stripes:form");
            requiredParentMap.put("stripes:hidden",   "stripes:form");
            requiredParentMap.put("stripes:button",   "stripes:form");

            // Options must be inside selects
            requiredParentMap.put("stripes:option",              "stripes:select");
            requiredParentMap.put("stripes:options-collection",  "stripes:select");
            requiredParentMap.put("stripes:options-enumeration", "stripes:select");

            // Error chunks must be inside the errors tag
            requiredParentMap.put("stripes:errors-header",    "stripes:errors");
            requiredParentMap.put("stripes:errors-footer",    "stripes:errors");
            requiredParentMap.put("stripes:individual-error", "stripes:errors");

            // LinkParams must be inside Links
            requiredParentMap.put("stripes:link-param", "stripes:link");

            // Now flip things around
            verbotenParentMap.put("stripes:form", "stripes:form");
        }

        /** Fetches the validation messages that were made when the page was parsed. */
        public List<ValidationMessage> getValidationMessages() {
            return validationMessages;
        }

        /**
         * Checks to see if the tag being started is contained within the required parent tag
         * type, and not contained with an invalid parent type.  If any errors are found, error
         * objects are created for providing the application developer with feedback.
         */
        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attributes) throws SAXException {

            // TODO: tomcat doesn't pass uri of localName.  When it does, fix this class up!

            String jspId = attributes.getValue("jsp:id");

            // Check for a required parent
            String requiredParent = requiredParentMap.get(qName);
            if (requiredParent != null && stack.search(requiredParent) < 0) {
                ValidationMessage message =
                    new ValidationMessage(jspId, "The tag [" + qName +
                            "] must be located within the body of the ["
                                    + requiredParent + "] tag.");
                validationMessages.add(message);
            }

            // Check for a verboten parent
            String verbotenParent = verbotenParentMap.get(qName);
            if (verbotenParent != null && stack.search(verbotenParent) > 0) {
                ValidationMessage message =
                        new ValidationMessage(jspId, "The tag [" + qName +
                                "] may never be located within the body of the ["
                                + verbotenParent + "] tag.");
                validationMessages.add(message);
            }

            // Finally push this tag onto the stack for the next go round
            stack.push(qName);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack.pop();
        }
    }
}
