package org.stripesframework.web.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.StripesTestFixture;


public class DefaultFormatterFactoryTest {

   @Test
   public void testFormatterForInterfaceSuperclass() throws Exception {
      DefaultFormatterFactory factory = new DefaultFormatterFactory();
      factory.init(StripesTestFixture.getDefaultConfiguration());
      factory.add(IfaceLevel1.class, IfaceLevel1Formatter.class);

      Formatter<?> formatter = factory.getFormatter(ImplementsIfaceWithSuperclasses.class, Locale.getDefault(), null, null);
      assertThat(formatter.getClass()).isSameAs(IfaceLevel1Formatter.class);
   }

   @Test
   public void testFormatterInterface() throws Exception {
      DefaultFormatterFactory factory = new DefaultFormatterFactory();
      factory.init(StripesTestFixture.getDefaultConfiguration());

      Locale locale = Locale.getDefault();
      Formatter<?> formatter;

      factory.add(X.class, XFormatter.class);

      formatter = factory.getFormatter(L.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(XFormatter.class);
      formatter = factory.getFormatter(M.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(XFormatter.class);
      formatter = factory.getFormatter(N.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(XFormatter.class);
      formatter = factory.getFormatter(O.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(XFormatter.class);
      formatter = factory.getFormatter(P.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(XFormatter.class);

      factory.add(Y.class, YFormatter.class);

      formatter = factory.getFormatter(L.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(XFormatter.class);
      formatter = factory.getFormatter(M.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(YFormatter.class);
      formatter = factory.getFormatter(N.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(YFormatter.class);
      formatter = factory.getFormatter(O.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(YFormatter.class);
      formatter = factory.getFormatter(P.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(YFormatter.class);

      factory.add(Z.class, ZFormatter.class);

      formatter = factory.getFormatter(L.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(XFormatter.class);
      formatter = factory.getFormatter(M.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(YFormatter.class);
      formatter = factory.getFormatter(N.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(ZFormatter.class);
      formatter = factory.getFormatter(O.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(YFormatter.class);
      formatter = factory.getFormatter(P.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(ZFormatter.class);
   }

   @Test
   public void testFormatterSuperclass() throws Exception {
      DefaultFormatterFactory factory = new DefaultFormatterFactory();
      factory.init(StripesTestFixture.getDefaultConfiguration());

      Locale locale = Locale.getDefault();
      Formatter<?> formatter;

      factory.add(A.class, AFormatter.class);

      formatter = factory.getFormatter(A.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(AFormatter.class);
      formatter = factory.getFormatter(B.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(AFormatter.class);
      formatter = factory.getFormatter(C.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(AFormatter.class);

      factory.add(B.class, BFormatter.class);

      formatter = factory.getFormatter(A.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(AFormatter.class);
      formatter = factory.getFormatter(B.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(BFormatter.class);
      formatter = factory.getFormatter(C.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(BFormatter.class);

      factory.add(C.class, CFormatter.class);

      formatter = factory.getFormatter(A.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(AFormatter.class);
      formatter = factory.getFormatter(B.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(BFormatter.class);
      formatter = factory.getFormatter(C.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(CFormatter.class);
   }

   @Test
   public void testFormatterSuperclassImplementsInterface() throws Exception {
      DefaultFormatterFactory factory = new DefaultFormatterFactory();
      factory.init(StripesTestFixture.getDefaultConfiguration());

      Locale locale = Locale.getDefault();
      Formatter<?> formatter;

      // simple test to get formatter for a superclass interface
      factory.add(X.class, XFormatter.class);
      factory.add(Y.class, YFormatter.class);
      factory.add(Z.class, ZFormatter.class);

      formatter = factory.getFormatter(SuperclassImplementsX.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(XFormatter.class);
      formatter = factory.getFormatter(SuperclassImplementsY.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(YFormatter.class);
      formatter = factory.getFormatter(SuperclassImplementsZ.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(ZFormatter.class);

      /*
       * test that if Z extends Y extends X and Y implements I and a formatter is registered for
       * both X and I then the formatter returned for Y and Z is the I formatter
       */
      factory = new DefaultFormatterFactory();
      factory.init(StripesTestFixture.getDefaultConfiguration());

      factory.add(SuperclassImplementsX.class, XFormatter.class); // mapping for base class
      factory.add(Y.class, YFormatter.class); // mapping for interface in the middle

      formatter = factory.getFormatter(SuperclassImplementsX.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(XFormatter.class);
      formatter = factory.getFormatter(SuperclassImplementsY.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(YFormatter.class);
      formatter = factory.getFormatter(SuperclassImplementsZ.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(YFormatter.class);
   }

   @Test
   public void testNullFormatterIsNeverBestMatch() throws Exception {
      DefaultFormatterFactory factory = new DefaultFormatterFactory();
      factory.init(StripesTestFixture.getDefaultConfiguration());

      Locale locale = Locale.getDefault();
      Formatter<?> formatter;

      // cause null formatter to be cached for B
      formatter = factory.getFormatter(B.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(ObjectFormatter.class);

      // then register formatter for A and try to get formatter for C
      factory.add(A.class, AFormatter.class);
      formatter = factory.getFormatter(C.class, locale, null, null);
      assertThat(formatter.getClass()).isSameAs(AFormatter.class);
   }

   public interface IfaceLevel1 {}


   public interface IfaceLevel2 extends IfaceLevel1 {}


   public interface IfaceLevel3 extends IfaceLevel2 {}


   public interface X {}


   public interface Y extends X {}


   public interface Z {}


   public static class A {}


   public static class AFormatter extends DummyFormatter<A> {}


   public static class B extends A {}


   public static class BFormatter extends DummyFormatter<B> {}


   public static class C extends B {}


   public static class CFormatter extends DummyFormatter<C> {}


   public static class DummyFormatter<T> implements Formatter<T> {

      @Override
      public String format( T input ) {
         return null;
      }

      @Override
      public void init() {
      }

      @Override
      public void setFormatPattern( String formatPattern ) {
      }

      @Override
      public void setFormatType( String formatType ) {
      }

      @Override
      public void setLocale( Locale locale ) {
      }
   }


   public static class IfaceLevel1Formatter extends DummyFormatter<IfaceLevel1> {}


   public static class ImplementsIfaceWithSuperclasses implements IfaceLevel3 {}


   public static class L implements X {}


   public static class M implements Y {}


   public static class N extends M implements Z {}


   public static class O extends L implements Y {}


   public static class P extends O implements Z {}


   public static class SuperclassImplementsX extends L {}


   public static class SuperclassImplementsY extends M {}


   public static class SuperclassImplementsZ extends N {}


   public static class XFormatter extends DummyFormatter<X> {}


   public static class YFormatter extends DummyFormatter<Y> {}


   public static class ZFormatter extends DummyFormatter<Z> {}
}
