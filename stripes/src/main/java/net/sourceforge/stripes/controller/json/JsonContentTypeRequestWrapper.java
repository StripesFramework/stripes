/*
 * Copyright 2015 Stripes Framework.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.controller.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.controller.ContentTypeRequestWrapper;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;

/**
 * This class is responsible for extracting parameters from the body of requests
 * which are of a JSON content type.
 *
 * @author Rick Grashel
 */
public class JsonContentTypeRequestWrapper implements ContentTypeRequestWrapper {

    private static final Log log = Log.getInstance(JsonContentTypeRequestWrapper.class);

    private Map< String, Set<String>> parameters = new HashMap< String, Set<String>>();

    public void build(HttpServletRequest request) throws IOException {

        log.debug("build() called.");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(request.getReader());

        if (rootNode.isArray()) {
            throw new StripesRuntimeException("The JSON requests bodies must start with an object brace and not an array.");
        }

        processNode(rootNode, null);
    }

    /**
     * This method will take a JSON node and process its conversion into a
     * parameter name and values.
     *
     * @param node - The JSON node to process.
     * @param parent - The parent path of this JSON node
     */
    private void processNode(JsonNode node, String parent) {

        log.debug("Processing node (", node.toString(), ")");

        if (node.isArray()) {
            for (int i = 0; i < node.size(); ++i) {
                String currentPath = parent + "[" + i + "]";
                JsonNode childNode = node.get(i);
                processNode(childNode, currentPath);
            }
        } else if (node.isObject()) {
            for (Iterator<String> i = node.fieldNames(); i.hasNext();) {
                String childFieldName = i.next();
                JsonNode childNode = node.get(childFieldName);
                String currentPath = (parent != null ? parent + "." + childFieldName : childFieldName);
                processNode(childNode, currentPath);
            }
        } else {
            String name = parent;
            Set<String> parameterValues = parameters.get(name);
            if (parameterValues == null) {
                parameterValues = new HashSet<String>();
            }
            parameterValues.add(node.asText());

            log.debug("Adding parameter (name=", name, ",value=", node.asText(), ")");

            parameters.put(name, parameterValues);
        }
    }

    /**
     * Returns the names of the parameters for this request.
     *
     * @return Names of the parameters for this request
     */
    public Enumeration<String> getParameterNames() {

        log.debug("Returning parameter names to a caller.");

        return new Enumeration<String>() {

            Iterator<String> iterator = parameters.keySet().iterator();

            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            public String nextElement() {
                return iterator.next();
            }
        };
    }

    /**
     * Returns a string array of the values for the passed parameter name.
     *
     * @param name - Parameter name to return values for
     * @return Array of values for the passed parameter name
     */
    public String[] getParameterValues(String name) {

        log.debug("Returning parameter value for name (", name, ") to a caller.");

        String[] returnValues = null;
        Set<String> values = parameters.get(name);

        if (values != null) {
            returnValues = values.toArray(new String[values.size()]);
        }

        log.debug("Returning parameter value (", returnValues, ") for name (", name, ") to a caller.");

        return returnValues;
    }

}
