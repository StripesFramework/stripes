package net.sourceforge.stripes.controller;

import java.util.Collections;
import java.util.List;

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.Log;

import org.testng.Assert;
import org.testng.annotations.Test;

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

        for (LifecycleStage stage : LifecycleStage.values()) {
            log.debug("Setting lifecycle stage: " + stage);
            ctx.setLifecycleStage(stage);

            List<Interceptor> interceptors = Collections.emptyList();
            ctx.setInterceptors(interceptors);

            ctx.wrap(new Interceptor() {
                public Resolution intercept(ExecutionContext context) throws Exception {
                    Assert.assertSame(ExecutionContext.currentContext(), ctx,
                            "The current context is not what was expected!");
                    return null;
                }
            });
        }

        log.debug("Lifecycle complete. Making sure current context is null.");
        Assert.assertNull(ExecutionContext.currentContext(),
                "The current context was not cleared at the end of the lifecycle.");
    }
}
