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

import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This resolution is intended to be used with Stripes REST action beans. This
 * type of resolution will take a Java object and serialize it to JSON
 * automatically.
 */
public class JsonResolution implements Resolution
{

    private final String rawJsonText;

    /**
     * This constructor should be used if the caller has already serialized the
     * object into JSON.
     *
     * @param rawJsonText - Raw text JSON string
     */
    public JsonResolution( String rawJsonText )
    {
        this.rawJsonText = rawJsonText;
    }

    /**
     * This constructor should be used if the caller wants to return an object
     * and have it automatically serialized into JSON.
     *
     * @param objectToSerialize - Object to serialize into JSON
     */
    public JsonResolution( Object objectToSerialize )
    {
        JsonBuilder builder = new JsonBuilder(objectToSerialize);
        this.rawJsonText = builder.build();
    }

    /**
     * Converts the object passed in to JSON and streams it back to the
     * client.
     */
    public void execute( HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        response.setContentType("application/json");
        Writer writer = response.getWriter();
        writer.write(rawJsonText);
        response.flushBuffer();
    }
}
