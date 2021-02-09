package net.sourceforge.stripes;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import net.sourceforge.stripes.mock.MockServletContext;


public class FilterEnabledTestBase {

   private static MockServletContext context;

   @AfterAll
   public static void closeCtx() {
      context.close();
   }

   @BeforeAll
   public static void initCtx() {
      context = StripesTestFixture.createServletContext();
   }

   public MockServletContext getMockServletContext() {
      return context;
   }
}
