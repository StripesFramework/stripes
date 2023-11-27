package net.sourceforge.stripes;

import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class FilterEnabledTestBase {

  private static MockServletContext context;

  @BeforeClass
  public static void initCtx() {
    context = StripesTestFixture.createServletContext();
  }

  @AfterClass
  public static void closeCtx() {
    context.close();
  }

  public MockServletContext getMockServletContext() {
    return context;
  }
}
