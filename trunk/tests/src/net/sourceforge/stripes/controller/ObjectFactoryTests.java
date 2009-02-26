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
package net.sourceforge.stripes.controller;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.config.TargetTypes;
import net.sourceforge.stripes.controller.ObjectFactory.ConstructorWrapper;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ObjectFactory} and {@link DefaultObjectFactory}.
 * 
 * @author Ben Gunter
 */
public class ObjectFactoryTests extends StripesTestFixture {
    public static final class Adder {
        private int a;
        private int b;

        public Adder(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int sum() {
            return a + b;
        }
    }

    public static class MyRunnable implements Runnable {
        public void run() {
        }
    }

    private static final Log log = Log.getInstance(ObjectFactoryTests.class);

    public void instantiateClasses(ObjectFactory factory, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            log.debug("Instantiating ", clazz);
            Object o = factory.newInstance(clazz);
            Assert.assertNotNull(o);
            Assert.assertSame(o.getClass(), clazz);
        }
    }

    public void instantiateInterfaces(ObjectFactory factory, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            log.debug("Instantiating ", clazz);
            Object o = factory.newInstance(clazz);
            log.debug("Implementation class is ", o.getClass().getName());
            Assert.assertNotNull(o);
            Assert.assertTrue(clazz.isAssignableFrom(o.getClass()));
        }
    }

    /** Test basic instantiation of classes. */
    @Test(groups = "fast")
    public void basic() {
        ObjectFactory factory = super.getDefaultConfiguration().getObjectFactory();
        instantiateClasses(factory, Object.class, String.class, Exception.class,
                StringBuilder.class);
    }

    /** Test instantiation of interfaces. */
    @Test(groups = "fast")
    public void interfaces() {
        ObjectFactory factory = getDefaultConfiguration().getObjectFactory();
        instantiateInterfaces(factory, Collection.class, List.class, Set.class, SortedSet.class,
                Queue.class, Map.class, SortedMap.class);
    }

    /** Test instantiation via constructor. */
    @Test(groups = "fast")
    public void constructor() {
        log.info("Instantiating ", Adder.class, " via constructor call");
        ConstructorWrapper<Adder> constructor = getDefaultConfiguration().getObjectFactory()
                .constructor(Adder.class, Integer.TYPE, Integer.TYPE);

        int a = 37, b = 91;
        Adder adder = constructor.newInstance(a, b);
        Assert.assertNotNull(adder);
        Assert.assertSame(adder.getClass(), Adder.class);
        Assert.assertEquals(adder.sum(), a + b);
    }

    /** Attempt to instantiate an interface that does not have a known implementing class. */
    @Test(groups = "fast", expectedExceptions = InstantiationException.class)
    public void missingInterfaceImpl() throws Throwable {
        try {
            log.debug("Attempting to instantiate ", Runnable.class, " expecting failure");
            getDefaultConfiguration().getObjectFactory().newInstance(Runnable.class);
        }
        catch (StripesRuntimeException e) {
            throw e.getCause();
        }
    }

    @Test(groups = "fast")
    public void customInterfaceImpl() {
        DefaultObjectFactory factory = new DefaultObjectFactory();
        factory.addImplementingClass(CharSequence.class, String.class);
        factory.addImplementingClass(List.class, LinkedList.class);
        factory.addImplementingClass(Runnable.class, MyRunnable.class);
        instantiateInterfaces(factory, CharSequence.class, List.class, Runnable.class);
        Assert.assertSame(factory.newInstance(List.class).getClass(), LinkedList.class);
    }

    /** Attempt to instantiate a class that does not have a no-arg constructor. */
    @Test(groups = "fast", expectedExceptions = InstantiationException.class)
    public void missingNoArgsConstructor() throws Throwable {
        try {
            log.debug("Attempting to instantiate ", Adder.class, " expecting failure");
            getDefaultConfiguration().getObjectFactory().newInstance(Adder.class);
        }
        catch (StripesRuntimeException e) {
            throw e.getCause();
        }
    }

    /** Alter an instance via {@link DefaultObjectFactory#postProcess(Object)}. */
    @Test(groups = "fast")
    public void postProcessMethod() {
        final String prefix = "Stripey!";
        DefaultObjectFactory factory = new DefaultObjectFactory() {
            @SuppressWarnings("unchecked")
            @Override
            protected <T> T postProcess(T object) {
                if (object instanceof String)
                    object = (T) (prefix + object);

                return object;
            }
        };

        final String expect = "TEST";
        String string;

        log.debug("Testing post-process method skips StringBuilder");
        string = factory.constructor(StringBuilder.class, String.class).newInstance(expect)
                .toString();
        log.debug("Got " + string);
        Assert.assertEquals(string, expect);

        log.debug("Testing post-process method via no-arg constructor");
        string = factory.newInstance(String.class);
        log.debug("Got " + string);
        Assert.assertEquals(string, prefix);

        log.debug("Testing post-process method via constructor with args");
        string = factory.constructor(String.class, String.class).newInstance(expect);
        log.debug("Got " + string);
        Assert.assertEquals(string, prefix + expect);
    }

    /** Alter an instance via {@link DefaultObjectFactory#postProcess(Object)}. */
    @Test(groups = "fast")
    public void classPostProcessor() {
        final String prefix = "Stripey!";
        @TargetTypes(String.class)
        class MyObjectPostProcessor implements ObjectPostProcessor {
            @SuppressWarnings("unchecked")
            public <T> T postProcess(T object) {
                log.debug("Altering '", object, "'");
                return (T) (prefix + object);
            }
        }

        DefaultObjectFactory factory = new DefaultObjectFactory();
        factory.addPostProcessor(new MyObjectPostProcessor());

        final String expect = "TEST";
        String string;

        log.debug("Testing post-processor impl skips StringBuilder");
        string = factory.constructor(StringBuilder.class, String.class).newInstance(expect)
                .toString();
        log.debug("Got " + string);
        Assert.assertEquals(string, expect);

        log.debug("Testing post-processor impl via no-arg constructor");
        string = factory.newInstance(String.class);
        log.debug("Got " + string);
        Assert.assertEquals(string, prefix);

        log.debug("Testing post-processor impl via constructor with args");
        string = factory.constructor(String.class, String.class).newInstance(expect);
        log.debug("Got " + string);
        Assert.assertEquals(string, prefix + expect);
    }

    /** Alter an instance via {@link DefaultObjectFactory#postProcess(Object)}. */
    @Test(groups = "fast")
    public void interfacePostProcessor() {
        final String prefix = "Stripey!";
        @TargetTypes(CharSequence.class)
        class MyObjectPostProcessor implements ObjectPostProcessor {
            @SuppressWarnings("unchecked")
            public <T> T postProcess(T object) {
                log.debug("Altering '", object, "'");
                return (T) (prefix + object);
            }
        }

        DefaultObjectFactory factory = new DefaultObjectFactory();
        factory.addImplementingClass(Runnable.class, MyRunnable.class);
        factory.addPostProcessor(new MyObjectPostProcessor());

        final String expect = "TEST";
        String string;

        log.debug("Testing post-processor impl handles StringBuilder");
        string = String.valueOf(factory.constructor(StringBuilder.class, String.class).newInstance(
                expect));
        log.debug("Got " + string);
        Assert.assertEquals(string, prefix + expect);

        log.debug("Testing post-processor impl via no-arg constructor");
        string = factory.newInstance(String.class);
        log.debug("Got " + string);
        Assert.assertEquals(string, prefix);

        log.debug("Testing post-processor impl via constructor with args");
        string = factory.constructor(String.class, String.class).newInstance(expect);
        log.debug("Got " + string);
        Assert.assertEquals(string, prefix + expect);

        log.debug("Testing post-processor does not handle Runnable");
        string = factory.newInstance(Runnable.class).getClass().getName();
        log.debug("Got " + string);
        Assert.assertEquals(string, MyRunnable.class.getName());
    }

    @Test(groups = "fast")
    public void multipleSequentialPostProcessors() {
        final AtomicInteger counter = new AtomicInteger(0);
        @TargetTypes(StringBuilder.class)
        class MyObjectPostProcessor implements ObjectPostProcessor {
            @SuppressWarnings("unchecked")
            public <T> T postProcess(T object) {
                log.debug("Altering '", object, "'");
                return (T) ((StringBuilder) object).append("Touched by ").append(
                        this.toString().replaceAll(".*@", "")).append(" (counter=").append(
                        counter.addAndGet(1)).append(") ... ");
            }
        }

        DefaultObjectFactory factory = new DefaultObjectFactory();
        for (int i = 0; i < 5; i++) {
            factory.addPostProcessor(new MyObjectPostProcessor());
        }
        log.debug("Testing multiple post-processors");
        StringBuilder buf = factory.newInstance(StringBuilder.class);
        log.debug("Got ", buf);
        Assert.assertEquals(counter.intValue(), 5);
    }
}
