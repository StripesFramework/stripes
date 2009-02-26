/* Copyright 2009 Ben Gunter
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
package net.sourceforge.stripes.controller;

/**
 * Allows for post-processing of objects created by {@link DefaultObjectFactory}. To register a
 * post-processor with the {@link ObjectFactory}, you must pass it to $$$. Implementations of this
 * interface must be thread-safe, as instances will be reused.
 * 
 * @author Ben Gunter
 */
public interface ObjectPostProcessor<T> {
    /**
     * Do whatever post-processing is necessary on the object and return it. It is not absolutely
     * required that this method return exactly the same object that was passed to it, but it is
     * strongly recommended.
     * 
     * @param object The object to be processed.
     * @return The object that was passed in.
     */
    T postProcess(T object);
}
