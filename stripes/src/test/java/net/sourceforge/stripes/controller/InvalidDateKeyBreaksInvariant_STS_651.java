package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.FilterEnabledTestBase;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.StripesTestFixture;

import java.util.Date;
import java.util.Map;

/**
 * reproduces http://www.stripesframework.org/jira/browse/STS-651
 */
public class InvalidDateKeyBreaksInvariant_STS_651 extends FilterEnabledTestBase {

    /** Helper method to create a roundtrip with the TestActionBean class. */
    protected MockRoundtrip getRoundtrip() {
        return new MockRoundtrip(getMockServletContext(), MapBindingTests.class);
    }

    @Test
    public void bindInvalidDateKeysInMapBreaksMapInvariant() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapDateDate['notadate']", "01/01/2000");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Map<Date, Date> mapDateDate = bean.getMapDateDate();
        try {
            // there should be only java.util.Date objects as keys of the map
            for (Date dateKey : mapDateDate.keySet()) {
                // if we go this far then it's ok, but we try to see
                // if the value if ok as well...
                Date dateValue = mapDateDate.get(dateKey);
                Assert.assertNotNull(dateValue);
            }
        }
        catch (ClassCastException e) {
            e.printStackTrace();
            Assert.fail("bad ! Map<Date,Date> contains a <String,?> entry, the map's invariant has been violated");
        }
    }
}
