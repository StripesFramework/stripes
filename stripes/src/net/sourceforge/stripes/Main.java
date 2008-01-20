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

        System.out.println("For more information on Stripes please visit http://stripesframework.org");
   }
}
