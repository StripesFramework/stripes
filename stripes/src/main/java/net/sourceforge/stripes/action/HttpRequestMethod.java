/*
 * Copyright 2015 Rick Grashel.
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

import java.util.Arrays;
import java.util.Collection;

/**
 * This is an enumeration of all of the valid HTTP request method types
 * which can be used for event handling within RestActionBean classes.
 *
 * @author Rick Grashel
 */
public enum HttpRequestMethod
{
    GET, POST, HEAD, PUT, DELETE, OPTIONS, TRACE, CONNECT, PATCH;

    /**
     * Returns a collection of all of the values for this enumeration.
     *
     * @return Collection of all values for this enumeration
     */
    public static Collection<HttpRequestMethod> all() {
        return Arrays.asList(values());
    }
}
