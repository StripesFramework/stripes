package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.action.UrlBinding;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

public class AnnotatedClassActionResolverTest {

    private AnnotatedClassActionResolver resolver = new AnnotatedClassActionResolver() {
        @Override
        protected Set<Class<? extends ActionBean>> findClasses() {
            Set<Class<? extends ActionBean>> classes = new HashSet<Class<? extends ActionBean>>();
            classes.add(SimpleActionBean.class);
            classes.add(OverloadedActionBean.class);
            classes.add(Container1.OverloadedActionBean.class);
            classes.add(Container2.OverloadedActionBean.class);
            return classes;
        }
    };

    @UrlBinding("/Simple.action")
    static class SimpleActionBean implements ActionBean {
        public void setContext(ActionBeanContext context) {
        }

        public ActionBeanContext getContext() {
            return null;
        }
    }

    @UrlBinding("/Overloaded.action")
    static class OverloadedActionBean implements ActionBean {
        public void setContext(ActionBeanContext context) {
        }

        public ActionBeanContext getContext() {
            return null;
        }
    }

    static class Container1 {
        @UrlBinding("/container1/Overloaded.action")
        static class OverloadedActionBean implements ActionBean {
            public void setContext(ActionBeanContext context) {
            }

            public ActionBeanContext getContext() {
                return null;
            }
        }
    }

    static class Container2 {
        @UrlBinding("/container2/Overloaded.action")
        static class OverloadedActionBean implements ActionBean {
            public void setContext(ActionBeanContext context) {
            }

            public ActionBeanContext getContext() {
                return null;
            }
        }
    }

    @BeforeTest
    public void setUp() throws Exception {
        resolver.init(null);
    }

    @Test(groups = "fast")
    public void findByName() {
        Class<? extends ActionBean> actionBean = resolver.getActionBeanByName("SimpleActionBean");
        Assert.assertNotNull(actionBean);
    }

    @Test(groups = "fast")
    public void multipleActionBeansWithSameSimpleName() {
        Class<? extends ActionBean> actionBean = resolver.getActionBeanByName("OverloadedActionBean");
        Assert.assertNull(actionBean);
    }
}