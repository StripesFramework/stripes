package org.stripesframework.web.controller;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.config.DontAutoLoad;
import org.stripesframework.web.controller.UrlBinding;
import org.stripesframework.web.controller.UrlBindingFactory;
import org.stripesframework.web.controller.UrlBindingParameter;

import org.stripesframework.web.exception.UrlBindingConflictException;
import org.stripesframework.web.util.bean.ParseException;


/**
 * Tests for {@link UrlBindingFactory}.
 *
 * @author Ben Gunter
 */
public class UrlBindingFactoryTests {

   private static UrlBindingFactory urlBindingFactory;

   @BeforeAll
   @SuppressWarnings("unchecked")
   public static void setupClass() {
      Class<? extends ActionBean>[] classes = new Class[] { ConflictActionBean1.class, ConflictActionBean2.class, ConflictActionBean3.class,
                                                            ConflictActionBean4.class, FooActionBean.class, FooActionBean1.class, FooActionBean2.class,
                                                            FooActionBean3.class, FooActionBean4.class, FooActionBean5.class, FooActionBean6.class,
                                                            FooActionBean7.class, FooActionBean8.class, SuffixActionBean1.class, SuffixActionBean2.class,
                                                            STS731ActionBean1.class, STS731ActionBean2.class, STS731ActionBean3.class };

      UrlBindingFactory factory = new UrlBindingFactory();
      for ( Class<? extends ActionBean> clazz : classes ) {
         factory.addBinding(clazz, UrlBindingFactory.parseUrlBinding(clazz));
      }

      urlBindingFactory = factory;
   }

   @Test
   public void testConflictDetectionIndependentOfClassLoadingOrder_failsRegardlessOfOrder() {
      UrlBindingFactory factory = new UrlBindingFactory();
      factory.addBinding(FooActionBean.class, UrlBindingFactory.parseUrlBinding(FooActionBean.class));
      factory.addBinding(FooActionBean2.class, UrlBindingFactory.parseUrlBinding(FooActionBean.class));

      Throwable throwable = catchThrowable(() -> factory.getBindingPrototype("/foo"));

      assertThat(throwable).isInstanceOf(UrlBindingConflictException.class);
   }

   @Test
   public void testConflictDetectionIndependentOfClassLoadingOrder_works() {
      UrlBindingFactory factory = new UrlBindingFactory();
      factory.addBinding(FooActionBean.class, UrlBindingFactory.parseUrlBinding(FooActionBean.class));
      factory.addBinding(FooActionBean2.class, UrlBindingFactory.parseUrlBinding(FooActionBean2.class));
      factory.addBinding(FooActionBean3.class, UrlBindingFactory.parseUrlBinding(FooActionBean3.class));
      factory.addBinding(FooActionBean4.class, UrlBindingFactory.parseUrlBinding(FooActionBean4.class));
      factory.addBinding(FooActionBean5.class, UrlBindingFactory.parseUrlBinding(FooActionBean5.class));
      factory.addBinding(FooActionBean6.class, UrlBindingFactory.parseUrlBinding(FooActionBean6.class));
      factory.addBinding(FooActionBean7.class, UrlBindingFactory.parseUrlBinding(FooActionBean7.class));
      factory.addBinding(FooActionBean8.class, UrlBindingFactory.parseUrlBinding(FooActionBean8.class));

      UrlBinding prototype = factory.getBindingPrototype("/foo");

      assertThat(prototype).isNotNull();
      assertThat(prototype.getBeanType()).isSameAs(FooActionBean.class);
   }

   @Test
   public void testConflictDetectionIndependentOfClassLoadingOrder_worksWithDifferentOrder() {
      UrlBindingFactory factory = new UrlBindingFactory();
      factory.addBinding(FooActionBean8.class, UrlBindingFactory.parseUrlBinding(FooActionBean8.class));
      factory.addBinding(FooActionBean7.class, UrlBindingFactory.parseUrlBinding(FooActionBean7.class));
      factory.addBinding(FooActionBean6.class, UrlBindingFactory.parseUrlBinding(FooActionBean6.class));
      factory.addBinding(FooActionBean5.class, UrlBindingFactory.parseUrlBinding(FooActionBean5.class));
      factory.addBinding(FooActionBean4.class, UrlBindingFactory.parseUrlBinding(FooActionBean4.class));
      factory.addBinding(FooActionBean3.class, UrlBindingFactory.parseUrlBinding(FooActionBean3.class));
      factory.addBinding(FooActionBean2.class, UrlBindingFactory.parseUrlBinding(FooActionBean2.class));
      factory.addBinding(FooActionBean.class, UrlBindingFactory.parseUrlBinding(FooActionBean.class));

      UrlBinding prototype = factory.getBindingPrototype("/foo");

      assertThat(prototype).isNotNull();
      assertThat(prototype.getBeanType()).isSameAs(FooActionBean.class);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testParser1() {
      Class<? extends ActionBean>[] classes = new Class[] { BadSyntaxActionBean1.class, BadSyntaxActionBean2.class, BadSyntaxActionBean3.class,
                                                            BadSyntaxActionBean4.class, BadSyntaxActionBean5.class, BadSyntaxActionBean6.class,
                                                            BadSyntaxActionBean7.class, BadSyntaxActionBean8.class };

      for ( Class<? extends ActionBean> clazz : classes ) {
         org.stripesframework.web.action.UrlBinding annotation = clazz.getAnnotation(org.stripesframework.web.action.UrlBinding.class);

         Throwable throwable = catchThrowable(() -> UrlBindingFactory.parseUrlBinding(clazz));

         assertThat(throwable).describedAs("Expected failure for parsing " + annotation.value()).isInstanceOf(ParseException.class);
      }
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testParser2() {
      Class<? extends ActionBean>[] classes = new Class[] { GoodSyntaxActionBean1.class, GoodSyntaxActionBean2.class, GoodSyntaxActionBean3.class,
                                                            GoodSyntaxActionBean4.class, GoodSyntaxActionBean5.class, GoodSyntaxActionBean6.class,
                                                            GoodSyntaxActionBean7.class, GoodSyntaxActionBean8.class, GoodSyntaxActionBean9.class,
                                                            GoodSyntaxActionBean10.class, GoodSyntaxActionBean11.class };

      for ( Class<? extends ActionBean> clazz : classes ) {
         org.stripesframework.web.action.UrlBinding annotation = clazz.getAnnotation(org.stripesframework.web.action.UrlBinding.class);

         UrlBinding binding = UrlBindingFactory.parseUrlBinding(clazz);

         assertThat(binding).isNotNull();
         assertThat(binding.toString()).isEqualTo(removeEscapes(annotation.value()), "Parsed expression is not the same as original expression");
      }
   }

   @Test
   public void testUrlBindingConflict() {
      checkBinding("/clash/not", ConflictActionBean4.class);
      checkBinding("/clash/not/", ConflictActionBean4.class);

      Throwable throwable = catchThrowable(() -> urlBindingFactory.getBinding("/clash"));

      assertThat(throwable).isInstanceOf(UrlBindingConflictException.class);
   }

   @Test
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

      // Suffixes, as reported in STS-731
      for ( String value : new String[] { "really-long", "long", "XX", "X" } ) {
         List<UrlBindingParameter> param;
         param = checkBinding(format("/sts731/%s/", value), STS731ActionBean1.class);
         assertThat(param.get(0).getValue()).isEqualTo(value);
         param = checkBinding(format("/sts731/%s/foo/", value), STS731ActionBean2.class);
         assertThat(param.get(0).getValue()).isEqualTo(value);
         param = checkBinding(format("/sts731/%s/bar/", value), STS731ActionBean3.class);
         assertThat(param.get(0).getValue()).isEqualTo(value);
      }
   }

   @Test
   @SuppressWarnings("unchecked")
   void testParser3() {
      // Check weird parameter names
      Class<? extends ActionBean>[] classes = new Class[] { GoodSyntaxActionBean7.class, GoodSyntaxActionBean8.class, GoodSyntaxActionBean9.class,
                                                            GoodSyntaxActionBean10.class, GoodSyntaxActionBean11.class };
      for ( Class<? extends ActionBean> clazz : classes ) {
         org.stripesframework.web.action.UrlBinding annotation = clazz.getAnnotation(org.stripesframework.web.action.UrlBinding.class);
         String value = annotation.value();

         UrlBinding binding = UrlBindingFactory.parseUrlBinding(clazz);
         assertThat(binding).isNotNull();
         assertThat(binding.getParameters()).hasSize(1);

         String pname = removeEscapes(value.substring(value.indexOf('{') + 1, value.lastIndexOf('}')));

         assertThat(binding.getParameters().get(0).getName()).isEqualTo(pname);
      }
   }

   private List<UrlBindingParameter> checkBinding( String uri, Class<? extends ActionBean> expected ) {
      UrlBinding binding = urlBindingFactory.getBinding(uri);
      assertThat(binding).describedAs("The uri \"" + uri + "\" matched nothing").isNotNull();
      assertThat(binding.getBeanType()).isSameAs(expected);
      return binding.getParameters();
   }

   private String removeEscapes( String s ) {
      return s.replaceAll("\\\\(.)", "$1");
   }

   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/{")
   public static class BadSyntaxActionBean1 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/\\")
   public static class BadSyntaxActionBean2 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/{a")
   public static class BadSyntaxActionBean3 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/{}")
   public static class BadSyntaxActionBean4 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/{=value}")
   public static class BadSyntaxActionBean5 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/{}}")
   public static class BadSyntaxActionBean6 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("{a}")
   public static class BadSyntaxActionBean7 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("")
   public static class BadSyntaxActionBean8 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/clash")
   public static class ConflictActionBean1 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/clash")
   public static class ConflictActionBean2 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/clash")
   public static class ConflictActionBean3 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/clash/not")
   public static class ConflictActionBean4 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo")
   public static class FooActionBean extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo/{a}")
   public static class FooActionBean1 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo/{a}/{b}")
   public static class FooActionBean2 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo/{a}/{b}/{c}")
   public static class FooActionBean3 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo/{a}/{b}/{c}/{d}")
   public static class FooActionBean4 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo/{a}/bar")
   public static class FooActionBean5 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo/{a}/bar/{c}/baz")
   public static class FooActionBean6 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo/{a}/{b}/{c}/{d}.action")
   public static class FooActionBean7 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo/goo/{a}")
   public static class FooActionBean8 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/")
   public static class GoodSyntaxActionBean1 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/{\\\\}")
   public static class GoodSyntaxActionBean10 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/{a\\=b}")
   public static class GoodSyntaxActionBean11 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo")
   public static class GoodSyntaxActionBean2 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo/{a}")
   public static class GoodSyntaxActionBean3 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/foo/{a}.action")
   public static class GoodSyntaxActionBean4 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/\\\\")
   public static class GoodSyntaxActionBean5 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/\\{")
   public static class GoodSyntaxActionBean6 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/{{}")
   public static class GoodSyntaxActionBean7 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/{\\}}")
   public static class GoodSyntaxActionBean8 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/syntax/{{\\}}")
   public static class GoodSyntaxActionBean9 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/sts731/{a}/")
   public static class STS731ActionBean1 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/sts731/{a}/foo/")
   public static class STS731ActionBean2 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/sts731/{a}/bar/")
   public static class STS731ActionBean3 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/suffix/{a}/{b}.action")
   public static class SuffixActionBean1 extends BaseActionBean {}


   @DontAutoLoad
   @org.stripesframework.web.action.UrlBinding("/suffix/{a}/{b}/{c}/{d}.action")
   public static class SuffixActionBean2 extends BaseActionBean {}


   private static abstract class BaseActionBean implements ActionBean {

      @Override
      public ActionBeanContext getContext() {
         return null;
      }

      @Override
      public void setContext( ActionBeanContext context ) {
      }
   }
}
