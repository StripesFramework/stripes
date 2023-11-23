/* Copyright 2008-2009 Ben Gunter
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.config.TargetTypes;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.util.TypeHandlerCache;

/**
 * An implementation of {@link ObjectFactory} that simply calls
 * Class.getDeclaredConstructor().newInstance() to obtain a new instance.
 *
 * @author Ben Gunter
 * @since Stripes 1.5.1
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultObjectFactory implements ObjectFactory {
  /**
   * An implementation of {@link ConstructorWrapper} that calls back to {@link
   * DefaultObjectFactory#newInstance(Constructor, Object...)} to instantiate a class.
   */
  public static class DefaultConstructorWrapper<T> implements ConstructorWrapper<T> {
    private final ObjectFactory factory;
    private final Constructor<T> constructor;

    /**
     * Wrap the given constructor.
     *
     * @param factory The object factory whose {@link ObjectFactory#newInstance(Constructor,
     *     Object...)} method will be called when invoking the constructor.
     * @param constructor The constructor to wrap.
     */
    public DefaultConstructorWrapper(ObjectFactory factory, Constructor<T> constructor) {
      this.factory = factory;
      this.constructor = constructor;
    }

    @Override
    public Constructor<T> getConstructor() {
      return constructor;
    }

    @Override
    public T newInstance(Object... args) {
      return factory.newInstance(constructor, args);
    }
  }

  private static final Log log = Log.getInstance(DefaultObjectFactory.class);

  /**
   * Holds a map of commonly used interface types (mostly collections) to a class that implements
   * the interface and will, by default, be instantiated when an instance of the interface is
   * needed.
   */
  protected final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();

  {
    interfaceImplementations.put(Collection.class, ArrayList.class);
    interfaceImplementations.put(List.class, ArrayList.class);
    interfaceImplementations.put(Set.class, HashSet.class);
    interfaceImplementations.put(SortedSet.class, TreeSet.class);
    interfaceImplementations.put(Queue.class, LinkedList.class);
    interfaceImplementations.put(Map.class, HashMap.class);
    interfaceImplementations.put(SortedMap.class, TreeMap.class);
  }

  private Configuration configuration;
  private TypeHandlerCache<List<ObjectPostProcessor>> postProcessors;

  @Override
  public void init(Configuration configuration) throws Exception {
    this.configuration = configuration;
  }

  /**
   * Get the {@link Configuration} that was passed into {@link #init(Configuration)}.
   *
   * @return The configuration.
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * Register a post-processor that will be allowed to manipulate instances of {@code targetType}
   * after they are created and before they are returned. The types to which the post-processor will
   * apply are determined by the value of the {@link TargetTypes} annotation on the class. If there
   * is no such annotation, then the post-processor will process all instances created by the object
   * factory.
   *
   * @param postProcessor The post-processor to use.
   */
  public synchronized void addPostProcessor(ObjectPostProcessor postProcessor) {
    // The cache will be null by default to indicate that there are no post-processors
    if (postProcessors == null) {
      postProcessors = new TypeHandlerCache<>();
    }

    // Determine target types from type arguments
    List<Class<?>> targetTypes = new ArrayList<>();
    Type[] typeArguments =
        ReflectUtil.getActualTypeArguments(postProcessor.getClass(), ObjectPostProcessor.class);
    if ((typeArguments != null)
        && (typeArguments.length == 1)
        && !typeArguments[0].equals(Object.class)) {
      if (typeArguments[0] instanceof Class) {
        targetTypes.add((Class<?>) typeArguments[0]);
      } else {
        log.warn(
            "Type parameter for non-abstract post-processor [",
            postProcessor.getClass().getName(),
            "] is not a class.");
      }
    }

    // Determine target types from annotation; if no annotation then process everything
    TargetTypes annotation = postProcessor.getClass().getAnnotation(TargetTypes.class);
    if (annotation != null) targetTypes.addAll(Arrays.asList(annotation.value()));

    // Default to Object
    if (targetTypes.isEmpty()) targetTypes.add(Object.class);

    // Register post-processor for each target type
    for (Class<?> targetType : targetTypes) {
      List<ObjectPostProcessor> list = postProcessors.getHandler(targetType);
      if (list == null) {
        list = new ArrayList<>();
        postProcessors.add(targetType, list);
      }
      log.debug(
          "Adding post-processor of type ",
          postProcessor.getClass().getName(),
          " for ",
          targetType);
      list.add(postProcessor);
    }

    postProcessor.setObjectFactory(this);
  }

  @Override
  public <T> T newInstance(Class<T> clazz) {
    try {
      if (clazz.isInterface()) return postProcess(newInterfaceInstance(clazz));
      else return postProcess(clazz.getDeclaredConstructor().newInstance());
    } catch (InstantiationException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      throw new StripesRuntimeException("Could not instantiate " + clazz, e);
    }
  }

  /**
   * Attempts to determine an implementing class for the interface provided and instantiate it using
   * a default constructor.
   *
   * @param <T> the type of the interface
   * @param interfaceType an interface (or abstract class) to make an instance of
   * @return an instance of the interface type supplied
   * @throws InstantiationException if no implementation type has been configured
   * @throws IllegalAccessException if thrown by the JVM during class instantiation
   */
  public <T> T newInterfaceInstance(Class<T> interfaceType)
      throws InstantiationException, IllegalAccessException {
    Class impl = getImplementingClass(interfaceType);
    if (impl == null) {
      throw new InstantiationException(
          "Stripes needed to instantiate a property who's declared type as an "
              + "interface (which obviously cannot be instantiated. The interface is not "
              + "one that Stripes is aware of, so no implementing class was known. The "
              + "interface type was: '"
              + interfaceType.getName()
              + "'. To fix this "
              + "you'll need to do one of three things. 1) Change the getter/setter methods "
              + "to use a concrete type so that Stripes can instantiate it. 2) in the bean's "
              + "setContext() method pre-instantiate the property so Stripes doesn't have to. "
              + "3) Bug the Stripes author ;)  If the interface is a JDK type it can easily be "
              + "fixed. If not, if enough people ask, a generic way to handle the problem "
              + "might get implemented.");
    } else {
      return newInstance((Class<T>) impl);
    }
  }

  /**
   * Looks up the default implementing type for the supplied interface. This is done based on a
   * static map of known common interface types and implementing classes.
   *
   * @param iface an interface for which an implementing class is needed
   * @return a Class object representing the implementing type, or null if one is not found
   */
  public Class<?> getImplementingClass(Class<?> iface) {
    return interfaceImplementations.get(iface);
  }

  /**
   * Register a class as the default implementation of an interface. The implementation class will
   * be returned from future calls to {@link #getImplementingClass(Class)} when the argument is
   * {@code iface}.
   *
   * @param <T> The type of the interface
   * @param iface The interface class
   * @param impl The implementation class
   */
  public <T> void addImplementingClass(Class<T> iface, Class<? extends T> impl) {
    if (!iface.isInterface())
      throw new IllegalArgumentException("Class " + iface.getName() + " is not an interface");
    else interfaceImplementations.put(iface, impl);
  }

  @Override
  public <T> T newInstance(
      Class<T> clazz, Class<?>[] constructorArgTypes, Object[] constructorArgs) {
    try {
      Constructor<T> constructor = clazz.getConstructor(constructorArgTypes);
      return postProcess(newInstance(constructor, constructorArgs));
    } catch (SecurityException | NoSuchMethodException | IllegalArgumentException e) {
      throw new StripesRuntimeException("Could not instantiate " + clazz, e);
    }
  }

  @Override
  public <T> T newInstance(Constructor<T> constructor, Object... params) {
    try {
      return postProcess(constructor.newInstance(params));
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new StripesRuntimeException("Could not invoke constructor " + constructor, e);
    }
  }

  @Override
  public <T> DefaultConstructorWrapper<T> constructor(Class<T> clazz, Class<?>... parameterTypes) {
    try {
      return new DefaultConstructorWrapper<>(this, clazz.getConstructor(parameterTypes));
    } catch (SecurityException | NoSuchMethodException e) {
      throw new StripesRuntimeException("Could not instantiate " + clazz, e);
    }
  }

  /**
   * Perform post-processing on objects created by {@link #newInstance(Class)} or {@link
   * #newInstance(Class, Class[], Object[])}. Subclasses that do not need to change the way objects
   * are instantiated but do need to do something to the objects before returning them may override
   * this method to achieve that.
   *
   * @param <T> The type of the object.
   * @param object A newly created object.
   * @return The given object, unchanged.
   */
  protected <T> T postProcess(T object) {
    if (postProcessors != null) {
      List<ObjectPostProcessor> list = postProcessors.getHandler(object.getClass());
      if (list != null) {
        for (ObjectPostProcessor postProcessor : list) {
          object = (T) postProcessor.postProcess(object);
        }
      }
    }

    return object;
  }
}
