/* Copyright 2009 Ben Gunter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.stripesframework.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.stripesframework.web.StripesTestFixture;
import org.stripesframework.web.controller.ObjectFactory.ConstructorWrapper;


/**
 * Unit tests for {@link ObjectFactory} and {@link DefaultObjectFactory}.
 *
 * @author Ben Gunter
 */
public class ObjectFactoryTests extends StripesTestFixture {

   /** Test basic instantiation of classes. */
   @Test
   public void basic() {
      ObjectFactory factory = getDefaultConfiguration().getObjectFactory();
      instantiateClasses(factory, Object.class, String.class, Exception.class, StringBuilder.class);
   }

   /** Alter an instance via {@link DefaultObjectFactory#postProcess(Object)}. */
   @Test
   public void classPostProcessor() {
      final String prefix = "Stripey!";
      class MyObjectPostProcessor implements ObjectPostProcessor<String> {

         @Override
         public String postProcess( String object ) {
            return (prefix + object);
         }

         @Override
         public void setObjectFactory( DefaultObjectFactory factory ) {}
      }

      DefaultObjectFactory factory = new DefaultObjectFactory();
      factory.addPostProcessor(new MyObjectPostProcessor());

      final String expect = "TEST";
      String string;

      string = factory.constructor(StringBuilder.class, String.class).newInstance(expect).toString();
      assertThat(string).isEqualTo(expect);

      string = factory.newInstance(String.class);
      assertThat(string).isEqualTo(prefix);

      string = factory.constructor(String.class, String.class).newInstance(expect);
      assertThat(string).isEqualTo(prefix + expect);
   }

   /** Test instantiation via constructor. */
   @Test
   public void constructor() {
      ConstructorWrapper<Adder> constructor = getDefaultConfiguration().getObjectFactory().constructor(Adder.class, Integer.TYPE, Integer.TYPE);

      int a = 37, b = 91;
      Adder adder = constructor.newInstance(a, b);
      assertThat(adder).isNotNull();
      assertThat(adder.getClass()).isSameAs(Adder.class);
      assertThat(adder.sum()).isEqualTo(a + b);
   }

   @Test
   public void customInterfaceImpl() {
      DefaultObjectFactory factory = new DefaultObjectFactory();
      factory.addImplementingClass(CharSequence.class, String.class);
      factory.addImplementingClass(List.class, LinkedList.class);
      factory.addImplementingClass(Runnable.class, MyRunnable.class);

      instantiateInterfaces(factory, CharSequence.class, List.class, Runnable.class);

      assertThat(factory.newInstance(List.class).getClass()).isSameAs(LinkedList.class);
   }

   public void instantiateClasses( ObjectFactory factory, Class<?>... classes ) {
      for ( Class<?> clazz : classes ) {
         Object o = factory.newInstance(clazz);
         assertThat(o).isNotNull();
         assertThat(o.getClass()).isSameAs(clazz);
      }
   }

   public void instantiateInterfaces( ObjectFactory factory, Class<?>... classes ) {
      for ( Class<?> clazz : classes ) {
         Object o = factory.newInstance(clazz);
         assertThat(o).isNotNull();
         assertThat(clazz.isAssignableFrom(o.getClass())).isTrue();
      }
   }

   /** Alter an instance via {@link DefaultObjectFactory#postProcess(Object)}. */
   @Test
   public void interfacePostProcessor() {
      final String prefix = "Stripey!";
      class MyObjectPostProcessor implements ObjectPostProcessor<CharSequence> {

         @Override
         public CharSequence postProcess( CharSequence object ) {
            return (prefix + object);
         }

         @Override
         public void setObjectFactory( DefaultObjectFactory factory ) {}
      }

      DefaultObjectFactory factory = new DefaultObjectFactory();
      factory.addImplementingClass(Runnable.class, MyRunnable.class);
      factory.addPostProcessor(new MyObjectPostProcessor());

      final String expect = "TEST";
      String string;

      string = String.valueOf(factory.constructor(StringBuilder.class, String.class).newInstance(expect));
      assertThat(string).isEqualTo(prefix + expect);

      string = factory.newInstance(String.class);
      assertThat(string).isEqualTo(prefix);

      string = factory.constructor(String.class, String.class).newInstance(expect);
      assertThat(string).isEqualTo(prefix + expect);

      string = factory.newInstance(Runnable.class).getClass().getName();
      assertThat(string).isEqualTo(MyRunnable.class.getName());
   }

   /** Test instantiation of interfaces. */
   @Test
   public void interfaces() {
      ObjectFactory factory = getDefaultConfiguration().getObjectFactory();
      instantiateInterfaces(factory, Collection.class, List.class, Set.class, SortedSet.class, Queue.class, Map.class, SortedMap.class);
   }

   /** Attempt to instantiate an interface that does not have a known implementing class. */
   @Test
   public void missingInterfaceImpl() {
      Throwable throwable = Assertions.catchThrowable(() -> getDefaultConfiguration().getObjectFactory().newInstance(Runnable.class));
      assertThat(throwable).isNotNull();
      assertThat(throwable.getCause()).isInstanceOf(InstantiationException.class);
   }

   /** Attempt to instantiate a class that does not have a no-arg constructor. */
   @Test
   public void missingNoArgsConstructor() {
      Throwable throwable = Assertions.catchThrowable(() -> getDefaultConfiguration().getObjectFactory().newInstance(Adder.class));
      assertThat(throwable).isNotNull();
      assertThat(throwable.getCause()).isInstanceOf(InstantiationException.class);
   }

   @Test
   public void multipleSequentialPostProcessors() {
      final AtomicInteger counter = new AtomicInteger(0);
      class MyObjectPostProcessor implements ObjectPostProcessor<StringBuilder> {

         @Override
         public StringBuilder postProcess( StringBuilder object ) {
            return object.append("Touched by ").append(toString().replaceAll(".*@", "")).append(" (counter=").append(counter.addAndGet(1)).append(") ... ");
         }

         @Override
         public void setObjectFactory( DefaultObjectFactory factory ) {}
      }

      DefaultObjectFactory factory = new DefaultObjectFactory();
      for ( int i = 0; i < 5; i++ ) {
         factory.addPostProcessor(new MyObjectPostProcessor());
      }
      factory.newInstance(StringBuilder.class);
      assertThat(counter.intValue()).isEqualTo(5);
   }

   /** Alter an instance via {@link DefaultObjectFactory#postProcess(Object)}. */
   @Test
   public void postProcessMethod() {
      final String prefix = "Stripey!";
      DefaultObjectFactory factory = new DefaultObjectFactory() {

         @SuppressWarnings("unchecked")
         @Override
         protected <T> T postProcess( T object ) {
            if ( object instanceof String ) {
               object = (T)(prefix + object);
            }

            return object;
         }
      };

      final String expect = "TEST";
      String string;

      string = factory.constructor(StringBuilder.class, String.class).newInstance(expect).toString();
      assertThat(string).isEqualTo(expect);

      string = factory.newInstance(String.class);
      assertThat(string).isEqualTo(prefix);

      string = factory.constructor(String.class, String.class).newInstance(expect);
      assertThat(string).isEqualTo(prefix + expect);
   }

   public static final class Adder {

      private final int a;
      private final int b;

      public Adder( int a, int b ) {
         this.a = a;
         this.b = b;
      }

      public int sum() {
         return a + b;
      }
   }


   public static class MyRunnable implements Runnable {

      @Override
      public void run() {
      }
   }
}
