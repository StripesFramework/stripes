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
 * Interface to be implemented by tags which wish to be able to receive parameters from
 * nested {@literal <stripes:param>} tags.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 * @see ParamTag
 */
public interface ParameterizableTag {
    /**
     * Adds a parameter to the tag.  It is up to the tag to determine whether the new value(s)
     * supplied supercede or add to previous values.  The value can be of any type, and tags
     * should handle Arrays and Collections gracefully.
     *
     * @param name the name of the parameter
     * @param valueOrValues either a scalar value, an array or a collection
     */
    void addParameter(String name, Object valueOrValues);
}
