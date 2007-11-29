/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.tag;

/**
 * A collection of static functions that are included in the Stripes tag library.  In most
 * cases these are not functions that are specific to Stripes, but simply functions that
 * make doing web development (especially with Java 5 constructs) easier.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class ElFunctions {

    /** Gets the name of the supplied enumerated value. */
    public static String name(Enum<?> e) {
        return e.name();
    }
}
