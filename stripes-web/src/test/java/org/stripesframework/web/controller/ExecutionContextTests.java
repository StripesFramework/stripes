package org.stripesframework.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.stripesframework.web.util.Log;


/**
 * Tests for {@link ExecutionContext}.
 *
 * @author Ben Gunter
 */
public class ExecutionContextTests {

   private static final Log log = Log.getInstance(ExecutionContextTests.class);

   @Test
   public void testCurrentContext() throws Exception {
      log.debug("Testing ExecutionContext.currentContext()");
      final ExecutionContext ctx = new ExecutionContext();

      for ( LifecycleStage stage : LifecycleStage.values() ) {
         log.debug("Setting lifecycle stage: " + stage);
         ctx.setLifecycleStage(stage);

         List<Interceptor> interceptors = Collections.emptyList();
         ctx.setInterceptors(interceptors);

         ctx.wrap(context -> {
            assertThat(ExecutionContext.currentContext()).describedAs("The current context is not what was expected!").isSameAs(ctx);
            return null;
         });
      }

      log.debug("Lifecycle complete. Making sure current context is null.");
      assertThat(ExecutionContext.currentContext()).describedAs("The current context was not cleared at the end of the lifecycle.").isNull();
   }
}
