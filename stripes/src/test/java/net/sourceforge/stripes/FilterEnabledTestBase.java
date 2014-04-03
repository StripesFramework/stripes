package net.sourceforge.stripes;

import net.sourceforge.stripes.mock.MockServletContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class FilterEnabledTestBase {

    private MockServletContext context;

    @BeforeClass
    public void initCtx() {
        context = StripesTestFixture.createServletContext();
    }

    @AfterClass
    public void closeCtx() {
        context.close();
    }

    public MockServletContext getMockServletContext() {
        return context;
    }
}
