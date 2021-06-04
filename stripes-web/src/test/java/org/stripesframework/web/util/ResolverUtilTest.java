package org.stripesframework.web.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.validation.BooleanTypeConverter;
import org.stripesframework.web.validation.DateTypeConverter;
import org.stripesframework.web.validation.LocalizableError;
import org.stripesframework.web.validation.ScopedLocalizableError;
import org.stripesframework.web.validation.SimpleError;
import org.stripesframework.web.validation.TypeConverter;


/**
 * Simple test case that tets out the basic functionality of the Resolver Util class.
 *
 * @author Tim Fennell
 */
public class ResolverUtilTest {

   @Test
   public void testFindExtensionsOfClass() {
      ResolverUtil<SimpleError> resolver = new ResolverUtil<>();
      resolver.findImplementations(SimpleError.class, "org.stripesframework.web");

      Set<Class<? extends SimpleError>> impls = resolver.getClasses();

      assertThat(impls.contains(LocalizableError.class)).describedAs("LocalizableError should have been found.").isTrue();
      assertThat(impls.contains(ScopedLocalizableError.class)).describedAs("ScopedLocalizableError should have been found.").isTrue();
      assertThat(impls.contains(SimpleError.class)).describedAs("SimpleError itself should have been found.").isTrue();
   }

   @Test
   public void testFindZeroImplementations() {
      ResolverUtil<ZeroImplementations> resolver = new ResolverUtil<>();
      resolver.findImplementations(ZeroImplementations.class, "org.stripesframework.web");

      Set<Class<? extends ZeroImplementations>> impls = resolver.getClasses();

      assertThat(impls).describedAs("There should not have been any implementations besides the interface itself.").containsExactly(ZeroImplementations.class);
   }

   @Test
   public void testMoreSpecificFind() {
      // Because the tests package depends on stripes, it's safe to assume that
      // there will be some TypeConverter subclasses in the classpath
      ResolverUtil<TypeConverter<?>> resolver = new ResolverUtil<>();
      resolver.findImplementations(TypeConverter.class, "org.stripesframework.web.validation");
      Set<Class<? extends TypeConverter<?>>> impls = resolver.getClasses();

      // Check on a few random converters
      assertThat(impls.contains(BooleanTypeConverter.class)).describedAs("BooleanTypeConverter went missing.").isTrue();
      assertThat(impls.contains(DateTypeConverter.class)).describedAs("DateTypeConverter went missing.").isTrue();
      assertThat(impls.contains(BooleanTypeConverter.class)).describedAs("ShortTypeConverter went missing.").isTrue();

      assertThat(impls.size()).describedAs("Did not find all the built in TypeConverters.").isGreaterThan(10);
   }

   @Test
   public void testSimpleFind() {
      // Because the tests package depends on stripes, it's safe to assume that
      // there will be some TypeConverter subclasses in the classpath
      ResolverUtil<TypeConverter<?>> resolver = new ResolverUtil<>();
      resolver.findImplementations(TypeConverter.class, "org");
      Set<Class<? extends TypeConverter<?>>> impls = resolver.getClasses();

      // Check on a few random converters
      assertThat(impls.contains(BooleanTypeConverter.class)).describedAs("BooleanTypeConverter went missing.").isTrue();
      assertThat(impls.contains(DateTypeConverter.class)).describedAs("DateTypeConverter went missing.").isTrue();
      assertThat(impls.contains(BooleanTypeConverter.class)).describedAs("ShortTypeConverter went missing.").isTrue();

      assertThat(impls.size()).describedAs("Did not find all the built in TypeConverters.").isGreaterThan(10);
   }

   /** Test interface used with the testFindZeroImplementatios() method. */
   private interface ZeroImplementations {}
}
