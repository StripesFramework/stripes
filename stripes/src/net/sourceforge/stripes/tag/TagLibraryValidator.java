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
 * @author Greg Hinkle
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

        static {
            requiredParentMap.put("stripes:text","stripes:form");
            requiredParentMap.put("stripes:radio","stripes:form");
            requiredParentMap.put("stripes:checkbox","stripes:form");
            requiredParentMap.put("stripes:select","stripes:form");
            requiredParentMap.put("stripes:file","stripes:form");
            requiredParentMap.put("stripes:textarea","stripes:form");
            requiredParentMap.put("stripes:submit","stripes:form");
            requiredParentMap.put("stripes:reset","stripes:form");
            requiredParentMap.put("stripes:hidden","stripes:form");
            requiredParentMap.put("stripes:button","stripes:form");

            requiredParentMap.put("stripes:option","stripes:select");
            requiredParentMap.put("stripes:options-collection","stripes:select");
            requiredParentMap.put("stripes:options-enumeration","stripes:select");


            requiredParentMap.put("stripes:errors-header","stripes:errors");
            requiredParentMap.put("stripes:errors-footer","stripes:errors");
            requiredParentMap.put("stripes:field-error","stripes:errors");
        }

        public List<ValidationMessage> getValidationMessages() {
            return validationMessages;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            String jspId = attributes.getValue("jsp:id");
            //TODO: Tomcat doesn't seem to put the uri in for this attribute as per the spec
            // (it should be http://java.sun.com/JSP/Page)
            stack.push(qName);
            String requiredParent = requiredParentMap.get(qName);
            if (requiredParent != null && stack.search(requiredParent) < 0) {
                ValidationMessage message = new ValidationMessage(jspId,
                        "The tag [" + qName + "] must be located within the body of the ["
                        + requiredParent + "] tag.");
                validationMessages.add(message);
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack.pop();
        }
    }
}
