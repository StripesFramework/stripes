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
