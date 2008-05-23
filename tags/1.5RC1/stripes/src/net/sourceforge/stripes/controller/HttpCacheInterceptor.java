/* Copyright 2007 Ben Gunter
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.HttpCache;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.Log;

/**
 * Looks for an {@link HttpCache} annotation on the event handler method, the {@link ActionBean}
 * class or the {@link ActionBean}'s superclasses. If an {@link HttpCache} is found, then the
 * appropriate response headers are set to control client-side caching.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 */
@Intercepts(LifecycleStage.ResolutionExecution)
public class HttpCacheInterceptor implements Interceptor {
    private static final class CacheKey {
        private Method method;
        private Class<?> beanClass;
        private int hashCode;

        /** Create a cache key for the given event handler method and {@link ActionBean} class. */
        public CacheKey(Method method, Class<? extends ActionBean> beanClass) {
            this.method = method;
            this.beanClass = beanClass;
            this.hashCode = method.hashCode() * 37 + beanClass.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            CacheKey that = (CacheKey) obj;
            return this.method.equals(that.method) && this.beanClass.equals(that.beanClass);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return beanClass.getName() + "." + method.getName() + "()";
        }
    }

    private static final Log logger = Log.getInstance(HttpCacheInterceptor.class);

    private Map<CacheKey, HttpCache> cache = new HashMap<CacheKey, HttpCache>(128);

    public Resolution intercept(ExecutionContext ctx) throws Exception {
        final ActionBean actionBean = ctx.getActionBean();
        final Method handler = ctx.getHandler();
        if (ctx.isResolutionFromHandler() && (actionBean != null) && (handler != null)) {
            final Class<? extends ActionBean> beanClass = actionBean.getClass();
            // if caching is disabled, then set the appropriate response headers
            logger.debug("Looking for ", HttpCache.class.getSimpleName(), " on ", beanClass
                    .getName(), ".", handler.getName(), "()");
            HttpCache annotation = getAnnotation(handler, beanClass);
            if (annotation != null) {
                HttpServletResponse response = ctx.getActionBeanContext().getResponse();
                if (annotation.allow()) {
                    long expires = annotation.expires();
                    if (expires != HttpCache.DEFAULT_EXPIRES) {
                        logger.debug("Response expires in ", expires, " seconds");
                        expires = expires * 1000 + System.currentTimeMillis();
                        response.setDateHeader("Expires", expires);
                    }
                }
                else {
                    logger.debug("Disabling client-side caching for response");
                    response.setDateHeader("Expires", 0);
                    response.setHeader("Cache-control", "no-cache");
                    response.setHeader("Pragma", "no-cache");
                }
            }
        }

        return ctx.proceed();
    }

    /**
     * Look for a {@link HttpCache} annotation on the method first and then on the class and its
     * superclasses.
     * 
     * @param method an event handler method
     * @param beanClass the class to inspect for annotations if none is found on the method
     * @return The first {@link HttpCache} annotation found. If none is found then null.
     */
    protected HttpCache getAnnotation(Method method, Class<? extends ActionBean> beanClass) {
        // check cache first
        CacheKey cacheKey = new CacheKey(method, beanClass);
        if (cache.containsKey(cacheKey)) {
            HttpCache annotation = cache.get(cacheKey);
            return annotation;
        }

        // not found in cache so figure it out
        HttpCache annotation = method.getAnnotation(HttpCache.class);
        if (annotation == null) {
            // search the method's class and its superclasses
            Class<?> clazz = beanClass;
            do {
                annotation = clazz.getAnnotation(HttpCache.class);
                clazz = clazz.getSuperclass();
            } while (clazz != null && annotation == null);
        }

        // check for weirdness
        if (annotation != null) {
            logger.debug("Found ", HttpCache.class.getSimpleName(), " for ", beanClass.getName(),
                    ".", method.getName(), "()");
            int expires = annotation.expires();
            if (annotation.allow() && expires != HttpCache.DEFAULT_EXPIRES && expires < 0) {
                logger.warn(HttpCache.class.getSimpleName(), " for ", beanClass.getName(), ".",
                        method.getName(), "() allows caching but expires in the past");
            }
            else if (!annotation.allow() && expires != HttpCache.DEFAULT_EXPIRES) {
                logger.warn(HttpCache.class.getSimpleName(), " for ", beanClass.getName(), ".",
                        method.getName(), "() disables caching but explicitly sets expires");
            }
        }

        // cache and return it
        cache.put(cacheKey, annotation);
        return annotation;
    }
}
