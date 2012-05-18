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
package net.sourceforge.stripes.config;

/**
 * Interface which is extended by all the major configurable chunks of Stripes.  Allows a
 * Configuration to instantiate and pass configuration to each of the main components in a
 * standardized manner.  It is expected that all ConfigurableComponents will have a public
 * no-arg constructor.
 *
 * @author Tim Fennell
 */
public interface ConfigurableComponent {

    /**
     * Invoked directly after instantiation to allow the configured component to perform
     * one time initialization.  Components are expected to fail loudly if they are not
     * going to be in a valid state after initialization.
     *
     * @param configuration the Configuration object being used by Stripes
     * @throws Exception should be thrown if the component cannot be configured well enough to use.
     */
    void init(Configuration configuration) throws Exception;
}
