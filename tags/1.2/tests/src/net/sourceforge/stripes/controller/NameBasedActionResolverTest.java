package net.sourceforge.stripes.controller;

import org.testng.Assert;
import org.testng.annotations.Test;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * 
 */
public class NameBasedActionResolverTest {
    private NameBasedActionResolver resolver = new NameBasedActionResolver();

    @Test(groups="fast")
    public void generateBinding() {
        String binding = this.resolver.getUrlBinding("foo.bar.web.admin.ControlCenterActionBean");
        Assert.assertEquals(binding, "/admin/ControlCenter.action");
    }

    @Test(groups="fast")
    public void generateBindingForNonPackagedClass() {
        String binding = this.resolver.getUrlBinding("ControlCenterActionBean");
        Assert.assertEquals(binding, "/ControlCenter.action");
    }

    @Test(groups="fast")
    public void generateBindingWithMultipleBasePackages() {
        String binding = this.resolver.getUrlBinding("foo.web.stripes.bar.www.ControlCenterActionBean");
        Assert.assertEquals(binding, "/ControlCenter.action");
    }

    @Test(groups="fast")
    public void generateBindingWithMultipleBasePackages2() {
        String binding = this.resolver.getUrlBinding("foo.web.stripes.www.admin.ControlCenterActionBean");
        Assert.assertEquals(binding, "/admin/ControlCenter.action");
    }

    @Test(groups="fast")
    public void generateBindingWithoutSuffix() {
        String binding = this.resolver.getUrlBinding("foo.web.stripes.www.admin.ControlCenter");
        Assert.assertEquals(binding, "/admin/ControlCenter.action");
    }

    @Test(groups="fast")
    public void generateBindingWithDifferentSuffix() {
        String binding = this.resolver.getUrlBinding("foo.web.stripes.www.admin.ControlCenterBean");
        Assert.assertEquals(binding, "/admin/ControlCenter.action");
    }

    @Test(groups="fast")
    public void generateBindingWithDifferentSuffix2() {
        String binding = this.resolver.getUrlBinding("foo.web.stripes.www.admin.ControlCenterAction");
        Assert.assertEquals(binding, "/admin/ControlCenter.action");
    }

    @Test(groups="fast")
    public void testWithAnnotatedClass() {
        String name = net.sourceforge.stripes.test.TestActionBean.class.getName();
        String binding = this.resolver.getUrlBinding(name);
        Assert.assertEquals(binding, "/test/Test.action");

        binding = this.resolver.getUrlBinding(net.sourceforge.stripes.test.TestActionBean.class);
        Assert.assertEquals(binding,
                            net.sourceforge.stripes.test.TestActionBean.class.
                                    getAnnotation(UrlBinding.class).value());
    }

}
