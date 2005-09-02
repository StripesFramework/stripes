/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.config;

/**
 * Interface which is extended by all the major configurable chunks of Stripes.  Allows a
 * Configration to instantiate and pass configuration to each of the main components in a
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
