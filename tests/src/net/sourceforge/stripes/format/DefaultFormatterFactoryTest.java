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

        factory.add(A.class, ATC.class);

        formatter = factory.getFormatter(A.class, locale, null, null);
        Assert.assertEquals(ATC.class, formatter.getClass());
        formatter = factory.getFormatter(B.class, locale, null, null);
        Assert.assertEquals(ATC.class, formatter.getClass());
        formatter = factory.getFormatter(C.class, locale, null, null);
        Assert.assertEquals(ATC.class, formatter.getClass());

        factory.add(B.class, BTC.class);

        formatter = factory.getFormatter(A.class, locale, null, null);
        Assert.assertEquals(ATC.class, formatter.getClass());
        formatter = factory.getFormatter(B.class, locale, null, null);
        Assert.assertEquals(BTC.class, formatter.getClass());
        formatter = factory.getFormatter(C.class, locale, null, null);
        Assert.assertEquals(BTC.class, formatter.getClass());

        factory.add(C.class, CTC.class);

        formatter = factory.getFormatter(A.class, locale, null, null);
        Assert.assertEquals(ATC.class, formatter.getClass());
        formatter = factory.getFormatter(B.class, locale, null, null);
        Assert.assertEquals(BTC.class, formatter.getClass());
        formatter = factory.getFormatter(C.class, locale, null, null);
        Assert.assertEquals(CTC.class, formatter.getClass());
    }

    @Test(groups = "fast")
    public void testFormatterInterface() throws Exception {
        DefaultFormatterFactory factory = new DefaultFormatterFactory();
        factory.init(new DefaultConfiguration());

        Locale locale = Locale.getDefault();
        Formatter<?> formatter;

        factory.add(X.class, XTC.class);

        formatter = factory.getFormatter(L.class, locale, null, null);
        Assert.assertEquals(XTC.class, formatter.getClass());
        formatter = factory.getFormatter(M.class, locale, null, null);
        Assert.assertEquals(XTC.class, formatter.getClass());
        formatter = factory.getFormatter(N.class, locale, null, null);
        Assert.assertEquals(XTC.class, formatter.getClass());
        formatter = factory.getFormatter(O.class, locale, null, null);
        Assert.assertEquals(XTC.class, formatter.getClass());
        formatter = factory.getFormatter(P.class, locale, null, null);
        Assert.assertEquals(XTC.class, formatter.getClass());

        factory.add(Y.class, YTC.class);

        formatter = factory.getFormatter(L.class, locale, null, null);
        Assert.assertEquals(XTC.class, formatter.getClass());
        formatter = factory.getFormatter(M.class, locale, null, null);
        Assert.assertEquals(YTC.class, formatter.getClass());
        formatter = factory.getFormatter(N.class, locale, null, null);
        Assert.assertEquals(YTC.class, formatter.getClass());
        formatter = factory.getFormatter(O.class, locale, null, null);
        Assert.assertEquals(YTC.class, formatter.getClass());
        formatter = factory.getFormatter(P.class, locale, null, null);
        Assert.assertEquals(YTC.class, formatter.getClass());

        factory.add(Z.class, ZTC.class);

        formatter = factory.getFormatter(L.class, locale, null, null);
        Assert.assertEquals(XTC.class, formatter.getClass());
        formatter = factory.getFormatter(M.class, locale, null, null);
        Assert.assertEquals(YTC.class, formatter.getClass());
        formatter = factory.getFormatter(N.class, locale, null, null);
        Assert.assertEquals(ZTC.class, formatter.getClass());
        formatter = factory.getFormatter(O.class, locale, null, null);
        Assert.assertEquals(YTC.class, formatter.getClass());
        formatter = factory.getFormatter(P.class, locale, null, null);
        Assert.assertEquals(ZTC.class, formatter.getClass());
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
        factory.add(A.class, ATC.class);
        formatter = factory.getFormatter(A.class, locale, null, null);
        Assert.assertEquals(ATC.class, formatter.getClass());
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

    public static class ATC extends DummyFormatter<A> {
    }

    public static class BTC extends DummyFormatter<B> {
    }

    public static class CTC extends DummyFormatter<C> {
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

    public static class XTC extends DummyFormatter<X> {
    }

    public static class YTC extends DummyFormatter<Y> {
    }

    public static class ZTC extends DummyFormatter<Z> {
    }
}
