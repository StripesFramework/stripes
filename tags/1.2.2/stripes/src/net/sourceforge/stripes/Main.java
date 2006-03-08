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
package net.sourceforge.stripes;

/**
 * Simple executable class that is used as the Main-Class in the Stripes jar. Outputs version
 * information and other information about the environment on which the jar is being
 * executed.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class Main {
    /** Main method that does what the class level javadoc states. */
    public static void main(String[] argv) {
        Package pkg = Main.class.getPackage();
        System.out.println("Stripes version \"" + pkg.getSpecificationVersion() + "\"" +
                           " (build " + pkg.getImplementationVersion() + ")");

        System.out.println(
                "Running on java version \"" + System.getProperty("java.version") + "\"" +
                " (build " + System.getProperty("java.runtime.version") + ")" +
                " from " + System.getProperty("java.vendor")
        );

        System.out.println(
                "Operating environment \"" + System.getProperty("os.name") + "\"" +
                " version " + System.getProperty("os.version") +
                " on " + System.getProperty("os.arch")
        );

        System.out.println("For more information on Stripes please visit http://stripes.mc4j.org");
   }
}
