/* Copyright 2008 Ben Gunter
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

import net.sourceforge.stripes.config.ConfigurableComponent;

/**
 * Used throughout Stripes to instantiate classes. The default implementation is
 * {@link DefaultObjectFactory}. You can specify an alternate implementation to use by setting the
 * {@code ObjectFactory.Class} initialization parameter for {@link StripesFilter} or by placing your
 * implementation in one of the packages named in {@code Extension.Packages}.
 * 
 * <pre>
 * &lt;init-param&gt;
 *  &lt;param-name&gt;ObjectFactory.Class&lt;/param-name&gt;
 *  &lt;param-value&gt;com.mycompany.stripes.ext.MyObjectFactory&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre>
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.1
 */
public interface ObjectFactory extends ConfigurableComponent {
    /**
     * Create a new instance of {@code clazz} and return it.
     * 
     * @param clazz The class to instantiate.
     * @return A new instances of the class.
     */
    <T> T newInstance(Class<T> clazz);
}