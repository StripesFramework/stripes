package net.sourceforge.stripes.format;

import java.util.Locale;

import net.sourceforge.stripes.config.DefaultConfiguration;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DefaultFormatterFactoryTest {
    @Test(groups = "fast")
    public void testFormatterSuperclass() throws Exception {
        DefaultFormatterFactory factory = new DefaultFormatterFactory();
        factory.init(new DefaultConfiguration());

        Locale locale = Locale.getDefault();
        Formatter<?> formatter;

        factory.add(A.class, AFormatter.class);

        formatter = factory.getFormatter(A.class, locale, null, null);
        Assert.assertEquals(AFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(B.class, locale, null, null);
        Assert.assertEquals(AFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(C.class, locale, null, null);
        Assert.assertEquals(AFormatter.class, formatter.getClass());

        factory.add(B.class, BFormatter.class);

        formatter = factory.getFormatter(A.class, locale, null, null);
        Assert.assertEquals(AFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(B.class, locale, null, null);
        Assert.assertEquals(BFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(C.class, locale, null, null);
        Assert.assertEquals(BFormatter.class, formatter.getClass());

        factory.add(C.class, CFormatter.class);

        formatter = factory.getFormatter(A.class, locale, null, null);
        Assert.assertEquals(AFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(B.class, locale, null, null);
        Assert.assertEquals(BFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(C.class, locale, null, null);
        Assert.assertEquals(CFormatter.class, formatter.getClass());
    }

    @Test(groups = "fast")
    public void testFormatterInterface() throws Exception {
        DefaultFormatterFactory factory = new DefaultFormatterFactory();
        factory.init(new DefaultConfiguration());

        Locale locale = Locale.getDefault();
        Formatter<?> formatter;

        factory.add(X.class, XFormatter.class);

        formatter = factory.getFormatter(L.class, locale, null, null);
        Assert.assertEquals(XFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(M.class, locale, null, null);
        Assert.assertEquals(XFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(N.class, locale, null, null);
        Assert.assertEquals(XFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(O.class, locale, null, null);
        Assert.assertEquals(XFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(P.class, locale, null, null);
        Assert.assertEquals(XFormatter.class, formatter.getClass());

        factory.add(Y.class, YFormatter.class);

        formatter = factory.getFormatter(L.class, locale, null, null);
        Assert.assertEquals(XFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(M.class, locale, null, null);
        Assert.assertEquals(YFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(N.class, locale, null, null);
        Assert.assertEquals(YFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(O.class, locale, null, null);
        Assert.assertEquals(YFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(P.class, locale, null, null);
        Assert.assertEquals(YFormatter.class, formatter.getClass());

        factory.add(Z.class, ZFormatter.class);

        formatter = factory.getFormatter(L.class, locale, null, null);
        Assert.assertEquals(XFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(M.class, locale, null, null);
        Assert.assertEquals(YFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(N.class, locale, null, null);
        Assert.assertEquals(ZFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(O.class, locale, null, null);
        Assert.assertEquals(YFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(P.class, locale, null, null);
        Assert.assertEquals(ZFormatter.class, formatter.getClass());
    }

    @Test(groups = "fast")
    public void testNullFormatterIsNeverBestMatch() throws Exception {
        DefaultFormatterFactory factory = new DefaultFormatterFactory();
        factory.init(new DefaultConfiguration());

        Locale locale = Locale.getDefault();
        Formatter<?> formatter;

        // cause null formatter to be cached for B
        formatter = factory.getFormatter(B.class, locale, null, null);
        Assert.assertNull(formatter);

        // then register formatter for A and try to get formatter for C
        factory.add(A.class, AFormatter.class);
        formatter = factory.getFormatter(C.class, locale, null, null);
        Assert.assertEquals(AFormatter.class, formatter.getClass());
    }

    @Test(groups = "fast")
    public void testFormatterSuperclassImplementsInterface() throws Exception {
        DefaultFormatterFactory factory = new DefaultFormatterFactory();
        factory.init(new DefaultConfiguration());

        Locale locale = Locale.getDefault();
        Formatter<?> formatter;

        // simple test to get formatter for a superclass interface
        factory.add(X.class, XFormatter.class);
        factory.add(Y.class, YFormatter.class);
        factory.add(Z.class, ZFormatter.class);

        formatter = factory.getFormatter(SuperclassImplementsX.class, locale, null, null);
        Assert.assertEquals(XFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(SuperclassImplementsY.class, locale, null, null);
        Assert.assertEquals(YFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(SuperclassImplementsZ.class, locale, null, null);
        Assert.assertEquals(ZFormatter.class, formatter.getClass());

        /*
         * test that if Z extends Y extends X and Y implements I and a formatter is registered for
         * both X and I then the formatter returned for Y and Z is the I formatter
         */
        factory = new DefaultFormatterFactory();
        factory.init(new DefaultConfiguration());

        factory.add(SuperclassImplementsX.class, XFormatter.class); // mapping for base class
        factory.add(Y.class, YFormatter.class); // mapping for interface in the middle

        formatter = factory.getFormatter(SuperclassImplementsX.class, locale, null, null);
        Assert.assertEquals(XFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(SuperclassImplementsY.class, locale, null, null);
        Assert.assertEquals(YFormatter.class, formatter.getClass());
        formatter = factory.getFormatter(SuperclassImplementsZ.class, locale, null, null);
        Assert.assertEquals(YFormatter.class, formatter.getClass());
    }

    public static class A {
    }

    public static class B extends A {
    }

    public static class C extends B {
    }

    public static class DummyFormatter<T> implements Formatter<T> {
        public String format(T input) {
            return null;
        }

        public void init() {
        }

        public void setFormatPattern(String formatPattern) {
        }

        public void setFormatType(String formatType) {
        }

        public void setLocale(Locale locale) {
        }
    }

    public static class AFormatter extends DummyFormatter<A> {
    }

    public static class BFormatter extends DummyFormatter<B> {
    }

    public static class CFormatter extends DummyFormatter<C> {
    }

    public interface X {
    }

    public interface Y extends X {
    }

    public interface Z {
    }

    public static class L implements X {
    }

    public static class M implements Y {
    }

    public static class N extends M implements Z {
    }

    public static class O extends L implements Y {
    }

    public static class P extends O implements Z {
    }

    public static class SuperclassImplementsX extends L {
    }

    public static class SuperclassImplementsY extends M {
    }

    public static class SuperclassImplementsZ extends N {
    }

    public static class XFormatter extends DummyFormatter<X> {
    }

    public static class YFormatter extends DummyFormatter<Y> {
    }

    public static class ZFormatter extends DummyFormatter<Z> {
    }
}
