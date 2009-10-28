package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.config.DontAutoLoad;
import net.sourceforge.stripes.exception.UrlBindingConflictException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.bean.ParseException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for {@link UrlBindingFactory}.
 * 
 * @author Ben Gunter
 */
public class UrlBindingFactoryTests {
    private static abstract class BaseActionBean implements ActionBean {
        public ActionBeanContext getContext() {
            return null;
        }

        public void setContext(ActionBeanContext context) {
        }
    }

    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/{")
    public static class BadSyntaxActionBean1 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/\\")
    public static class BadSyntaxActionBean2 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/{a")
    public static class BadSyntaxActionBean3 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/{}")
    public static class BadSyntaxActionBean4 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/{=value}")
    public static class BadSyntaxActionBean5 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/{}}")
    public static class BadSyntaxActionBean6 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("{a}")
    public static class BadSyntaxActionBean7 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("")
    public static class BadSyntaxActionBean8 extends BaseActionBean {}

    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/")
    public static class GoodSyntaxActionBean1 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo")
    public static class GoodSyntaxActionBean2 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo/{a}")
    public static class GoodSyntaxActionBean3 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo/{a}.action")
    public static class GoodSyntaxActionBean4 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/\\\\")
    public static class GoodSyntaxActionBean5 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/\\{")
    public static class GoodSyntaxActionBean6 extends BaseActionBean {}
    @DontAutoLoad  @net.sourceforge.stripes.action.UrlBinding("/syntax/{{}")
    public static class GoodSyntaxActionBean7 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/{\\}}")
    public static class GoodSyntaxActionBean8 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/{{\\}}")
    public static class GoodSyntaxActionBean9 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/{\\\\}")
    public static class GoodSyntaxActionBean10 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/syntax/{a\\=b}")
    public static class GoodSyntaxActionBean11 extends BaseActionBean {}

    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/clash")
    public static class ConflictActionBean1 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/clash")
    public static class ConflictActionBean2 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/clash")
    public static class ConflictActionBean3 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/clash/not")
    public static class ConflictActionBean4 extends BaseActionBean {}

    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo")
    public static class FooActionBean extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo/{a}")
    public static class FooActionBean1 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo/{a}/{b}")
    public static class FooActionBean2 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo/{a}/{b}/{c}")
    public static class FooActionBean3 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo/{a}/{b}/{c}/{d}")
    public static class FooActionBean4 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo/{a}/bar")
    public static class FooActionBean5 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo/{a}/bar/{c}/baz")
    public static class FooActionBean6 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo/{a}/{b}/{c}/{d}.action")
    public static class FooActionBean7 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/foo/goo/{a}")
    public static class FooActionBean8 extends BaseActionBean {}

    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/suffix/{a}/{b}.action")
    public static class SuffixActionBean1 extends BaseActionBean {}
    @DontAutoLoad @net.sourceforge.stripes.action.UrlBinding("/suffix/{a}/{b}/{c}/{d}.action")
    public static class SuffixActionBean2 extends BaseActionBean {}
    
    private static final Log log = Log.getInstance(UrlBindingFactoryTests.class);
    private static UrlBindingFactory urlBindingFactory;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public void setupClass() {
        Class<? extends ActionBean>[] classes = new Class[] { ConflictActionBean1.class,
                ConflictActionBean2.class, ConflictActionBean3.class, ConflictActionBean4.class,
                FooActionBean.class, FooActionBean1.class, FooActionBean2.class,
                FooActionBean3.class, FooActionBean4.class, FooActionBean5.class,
                FooActionBean6.class, FooActionBean7.class, FooActionBean8.class,
                SuffixActionBean1.class, SuffixActionBean2.class };

        UrlBindingFactory factory = new UrlBindingFactory();
        for (Class<? extends ActionBean> clazz : classes) {
            log.debug("Parsing and adding @UrlBinding for ", clazz);
            factory.addBinding(clazz, UrlBindingFactory.parseUrlBinding(clazz));
        }

        urlBindingFactory = factory;
    }

    private void checkBinding(String uri, Class<? extends ActionBean> expected) {
        log.debug("Checking that ", uri, " maps to ", expected);
        UrlBinding binding = urlBindingFactory.getBinding(uri);
        Assert.assertNotNull(binding);
        Assert.assertSame(binding.getBeanType(), expected);
    }

    @Test(groups = "fast")
    @SuppressWarnings("unchecked")
    public void testParser1() {
        Class<? extends ActionBean>[] classes = new Class[] { BadSyntaxActionBean1.class,
                BadSyntaxActionBean2.class, BadSyntaxActionBean3.class, BadSyntaxActionBean4.class,
                BadSyntaxActionBean5.class, BadSyntaxActionBean6.class, BadSyntaxActionBean7.class,
                BadSyntaxActionBean8.class };

        for (Class<? extends ActionBean> clazz : classes) {
            net.sourceforge.stripes.action.UrlBinding annotation = clazz
                    .getAnnotation(net.sourceforge.stripes.action.UrlBinding.class);
            log.debug("Parsing URL binding ", annotation.value(), ", expecting failure");
            try {
                UrlBindingFactory.parseUrlBinding(clazz);
                Assert.assertTrue(false, "Expected parse exception but did not get one");
            }
            catch (ParseException e) {
                log.debug("As expected: ", e.getMessage());
            }
        }
    }

    private String removeEscapes(String s) {
        return s.replaceAll("\\\\(.)", "$1");
    }

    @Test(groups = "fast")
    @SuppressWarnings("unchecked")
    public void testParser2() {
        Class<? extends ActionBean>[] classes = new Class[] { GoodSyntaxActionBean1.class,
                GoodSyntaxActionBean2.class, GoodSyntaxActionBean3.class,
                GoodSyntaxActionBean4.class, GoodSyntaxActionBean5.class,
                GoodSyntaxActionBean6.class, GoodSyntaxActionBean7.class,
                GoodSyntaxActionBean8.class, GoodSyntaxActionBean9.class,
                GoodSyntaxActionBean10.class, GoodSyntaxActionBean11.class };

        for (Class<? extends ActionBean> clazz : classes) {
            net.sourceforge.stripes.action.UrlBinding annotation = clazz
                    .getAnnotation(net.sourceforge.stripes.action.UrlBinding.class);
            log.debug("Parsing URL binding ", annotation.value());
            UrlBinding binding = UrlBindingFactory.parseUrlBinding(clazz);
            log.debug("Expression parsed to ", binding);
            Assert.assertNotNull(binding);
            Assert.assertEquals(binding.toString(), removeEscapes(annotation.value()),
                    "Parsed expression is not the same as original expression");
        }

        // Check weird parameter names
        classes = new Class[] { GoodSyntaxActionBean7.class, GoodSyntaxActionBean8.class,
                GoodSyntaxActionBean9.class, GoodSyntaxActionBean10.class,
                GoodSyntaxActionBean11.class };
        for (Class<? extends ActionBean> clazz : classes) {
            net.sourceforge.stripes.action.UrlBinding annotation = clazz
                    .getAnnotation(net.sourceforge.stripes.action.UrlBinding.class);
            String value = annotation.value();
            log.debug("Checking URL binding parameters for ", value);
            UrlBinding binding = UrlBindingFactory.parseUrlBinding(clazz);
            Assert.assertEquals(binding.getParameters().size(), 1,
                    "Was expecting exactly one parameter");
            String pname = removeEscapes(value.substring(value.indexOf('{') + 1, value
                    .lastIndexOf('}')));
            log.debug("Parameter name is ", pname);
            Assert.assertEquals(binding.getParameters().get(0).getName(), pname);
        }
    }

    @Test(groups = "fast", expectedExceptions = UrlBindingConflictException.class)
    public void testUrlBindingConflict() {
        checkBinding("/clash/not", ConflictActionBean4.class);
        checkBinding("/clash/not/", ConflictActionBean4.class);
        urlBindingFactory.getBinding("/clash");
    }

    @Test(groups = "fast")
    public void testUrlBindings() {
        // No extensions
        checkBinding("/foo", FooActionBean.class);
        checkBinding("/foo/", FooActionBean.class);
        checkBinding("/foo/1", FooActionBean1.class);
        checkBinding("/foo/1/", FooActionBean2.class);
        checkBinding("/foo/1/2", FooActionBean2.class);
        checkBinding("/foo/1/2/", FooActionBean3.class);
        checkBinding("/foo/1/2/3", FooActionBean3.class);
        checkBinding("/foo/1/2/3/4", FooActionBean4.class);
        checkBinding("/foo/1/2/3/4/", FooActionBean4.class);

        // With a suffix mixed in
        checkBinding("/foo.action", FooActionBean7.class);
        checkBinding("/foo/.action", FooActionBean7.class);
        checkBinding("/foo/1.action", FooActionBean7.class);
        checkBinding("/foo/1/.action", FooActionBean7.class);
        checkBinding("/foo/1/2.action", FooActionBean7.class);
        checkBinding("/foo/1/2/.action", FooActionBean7.class);
        checkBinding("/foo/1/2/3.action", FooActionBean7.class);
        checkBinding("/foo/1/2/3/.action", FooActionBean7.class);
        checkBinding("/foo/1/2/3/4.action", FooActionBean7.class);
        checkBinding("/foo/1/2/3/4/.action", FooActionBean7.class);

        // With literals mixed in
        checkBinding("/foo/1/bar", FooActionBean5.class);
        checkBinding("/foo/1/bar/", FooActionBean3.class); // #3 matches with more components than #6
        checkBinding("/foo/1/bar/2", FooActionBean3.class); // #3 matches with more components than #6
        checkBinding("/foo/1/bar/2/", FooActionBean4.class); // #4 matches with more components than #6 or #3
        checkBinding("/foo/1/bar/2/baz", FooActionBean6.class);
        checkBinding("/foo/1/bar/2/baz/", FooActionBean6.class);

        // Suffix conflict resolution
        checkBinding("/suffix/1.action", SuffixActionBean1.class);
        checkBinding("/suffix/1/.action", SuffixActionBean1.class);
        checkBinding("/suffix/1/2.action", SuffixActionBean1.class);
        checkBinding("/suffix/1/2/.action", SuffixActionBean2.class);
        checkBinding("/suffix/1/2/3.action", SuffixActionBean2.class);
        checkBinding("/suffix/1/2/3/.action", SuffixActionBean2.class);
        checkBinding("/suffix/1/2/3/4.action", SuffixActionBean2.class);
        checkBinding("/suffix/1/2/3/4/.action", SuffixActionBean2.class);

        // Prefix overrides everything else
        checkBinding("/foo/goo", FooActionBean8.class);
        checkBinding("/foo/goo/", FooActionBean8.class);
        checkBinding("/foo/goo/1", FooActionBean8.class);
        checkBinding("/foo/goo/1/", FooActionBean8.class);
        checkBinding("/foo/goo/1/2", FooActionBean8.class);
    }

    @Test(groups = "fast")
    public void testConflictDetectionIndependentOfClassLoadingOrder() {
        UrlBindingFactory factory;
        UrlBinding prototype;

        // This order works
        factory = new UrlBindingFactory();
        factory.addBinding(FooActionBean.class, UrlBindingFactory.parseUrlBinding(FooActionBean.class));
        factory.addBinding(FooActionBean2.class, UrlBindingFactory.parseUrlBinding(FooActionBean2.class));
        factory.addBinding(FooActionBean3.class, UrlBindingFactory.parseUrlBinding(FooActionBean3.class));
        factory.addBinding(FooActionBean4.class, UrlBindingFactory.parseUrlBinding(FooActionBean4.class));
        factory.addBinding(FooActionBean5.class, UrlBindingFactory.parseUrlBinding(FooActionBean5.class));
        factory.addBinding(FooActionBean6.class, UrlBindingFactory.parseUrlBinding(FooActionBean6.class));
        factory.addBinding(FooActionBean7.class, UrlBindingFactory.parseUrlBinding(FooActionBean7.class));
        factory.addBinding(FooActionBean8.class, UrlBindingFactory.parseUrlBinding(FooActionBean8.class));
        prototype = factory.getBindingPrototype("/foo");
        Assert.assertNotNull(prototype);
        Assert.assertSame(prototype.getBeanType(), FooActionBean.class);

        // This order was failing
        factory = new UrlBindingFactory();
        factory.addBinding(FooActionBean8.class, UrlBindingFactory.parseUrlBinding(FooActionBean8.class));
        factory.addBinding(FooActionBean7.class, UrlBindingFactory.parseUrlBinding(FooActionBean7.class));
        factory.addBinding(FooActionBean6.class, UrlBindingFactory.parseUrlBinding(FooActionBean6.class));
        factory.addBinding(FooActionBean5.class, UrlBindingFactory.parseUrlBinding(FooActionBean5.class));
        factory.addBinding(FooActionBean4.class, UrlBindingFactory.parseUrlBinding(FooActionBean4.class));
        factory.addBinding(FooActionBean3.class, UrlBindingFactory.parseUrlBinding(FooActionBean3.class));
        factory.addBinding(FooActionBean2.class, UrlBindingFactory.parseUrlBinding(FooActionBean2.class));
        factory.addBinding(FooActionBean.class, UrlBindingFactory.parseUrlBinding(FooActionBean.class));
        factory.getBindingPrototype("/foo");
        Assert.assertNotNull(prototype);
        Assert.assertSame(prototype.getBeanType(), FooActionBean.class);

        // And this should still fail, regardless of order
        factory = new UrlBindingFactory();
        factory.addBinding(FooActionBean.class, UrlBindingFactory.parseUrlBinding(FooActionBean.class));
        factory.addBinding(FooActionBean2.class, UrlBindingFactory.parseUrlBinding(FooActionBean.class));
        try {
            factory.getBindingPrototype("/foo");
            Assert.assertTrue(false, "A URL binding conflict was expected but it didn't happen!");
        }
        catch (UrlBindingConflictException e) {
            log.debug("Got expected URL binding conflict");
        }
    }
}
