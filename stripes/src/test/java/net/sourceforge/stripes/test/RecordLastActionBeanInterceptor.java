package net.sourceforge.stripes.test;

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.*;

/**
 * Created by shane on 23/9/16.
 */
@Intercepts(LifecycleStage.RequestComplete)
public class RecordLastActionBeanInterceptor implements Interceptor {

    @Override
    public Resolution intercept(ExecutionContext context) throws Exception {
        context.getActionBeanContext().getRequest().setAttribute(StripesConstants.REQ_ATTR_LAST_ACTION_BEAN, context.getActionBean());
        return null;
    }

}
