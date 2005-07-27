package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.exception.StripesJspException;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 25, 2005 Time: 7:03:00 AM To change this
 * template use File | Settings | File Templates.
 */
public interface PopulationStrategy {
    Object getValue(InputTagSupport tag) throws StripesJspException ;
}
