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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import net.sourceforge.stripes.StripesTestFixture;
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

    private static final Log log = Log.getInstance(ObjectFactoryTests.class);

    public static void main(String[] args) {
        ObjectFactoryTests tests = new ObjectFactoryTests();
        tests.postProcess();
    }

    public void instantiateClasses(ObjectFactory factory, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            log.debug("Instantiating ", clazz);
            Object o = factory.newInstance(clazz);
            Assert.assertNotNull(o);
            Assert.assertSame(clazz, o.getClass());
        }
    }

    public void instantiateInterfaces(ObjectFactory factory, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            log.debug("Instantiating ", clazz);
            Object o = factory.newInstance(clazz);
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
        Assert.assertSame(Adder.class, adder.getClass());
        Assert.assertEquals(a + b, adder.sum());
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
    public void postProcess() {
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

        log.debug("Testing post-process skips StringBuilder");
        string = factory.constructor(StringBuilder.class, String.class).newInstance(expect)
                .toString();
        log.debug("Got " + string);
        Assert.assertEquals(expect, string);

        log.debug("Testing post-process via no-arg constructor");
        string = factory.newInstance(String.class);
        log.debug("Got " + string);
        Assert.assertEquals(prefix, string);

        log.debug("Testing post-process via constructor with args");
        string = factory.constructor(String.class, String.class).newInstance(expect);
        log.debug("Got " + string);
        Assert.assertEquals(prefix + expect, string);
    }
}
