/*
 * Copyright 2014 Rick Grashel.
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
package net.sourceforge.stripes.action;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import net.sourceforge.stripes.ajax.JavaScriptBuilder;
import net.sourceforge.stripes.exception.StripesRuntimeException;

/**
 * This class converts an object to JSON. It uses the same conventions as
 * JavaScriptBuilder.
 *
 * @author Rick Grashel
 */
public class JsonBuilder
{

    private JavaScriptBuilder javascriptBuilder;

    /**
     * Constructs a new JsonBuilder object which is used to convert
     * the passed Java object into JSON -- exluding the optional list
     * of objects passed.
     * 
     * @param root - Root object to convert to JSON
     * @param objectsToExclude - Objects to exclude from the resulting JSON
     */
    public JsonBuilder( Object root, Object... objectsToExclude )
    {
        // Construct the Stripes JavascriptBuilder.  This will be used to
        // create a Javascript object which will be converted to JSON.
        javascriptBuilder = new JavaScriptBuilder(root, objectsToExclude);
    }

    /**
     * Adds one or more properties to the list of property to exclude when
     * translating to JSON.
     *
     * @param property one or more property names to be excluded
     */
    public void addPropertyExclusion( String... property )
    {
        this.javascriptBuilder = javascriptBuilder.addPropertyExclusion(property);
    }

    /**
     * Adds one or more properties to the list of properties to exclude when
     * translating to JSON.
     *
     * @param clazz one or more classes to exclude
     */
    public void addClassExclusion( Class<?>... clazz )
    {
        this.javascriptBuilder = javascriptBuilder.addClassExclusion(clazz);
    }

    /**
     * Causes the JsonBuilder to navigate the properties of the supplied object
     * and convert them to JSON
     *
     * @return JSON version of the Java object supplied to the builder.
     */
    public String build()
    {
        try
        {
            // Create the Javascript enging to be used for JSON conversion
            String javascriptString = javascriptBuilder.build();
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");

            // Convert the root object to Javascript, and then take the 
            // Javascript representation and convert it to JSON
            // using the Rhino scripting engine within the JDK
            StringBuilder jsonStringBuilder = new StringBuilder();
            engine.put("jsonStringBuilder", jsonStringBuilder);
            engine.eval(javascriptString + "; jsonString = JSON.stringify(" + javascriptBuilder.getRootVariableName() + ", undefined, 2); jsonStringBuilder.append( jsonString );");

            return jsonStringBuilder.toString();
        }
        catch ( Exception e )
        {
            throw new StripesRuntimeException("Could not build JSON for object. An "
                    + "exception was thrown while trying to convert a property from Java to "
                    + "JSON. The object being converted is: " + javascriptBuilder.getRootVariableName() );
        }

    }

}
