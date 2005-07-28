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
