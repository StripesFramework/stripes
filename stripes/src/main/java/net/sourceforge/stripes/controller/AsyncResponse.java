package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Base class for Servlet3-style asynchronous processing. Instances of
 * this class are passed by Stripes to asynchronous event handlers,
 * and allow to complete the asynchronous processing.
 *
 * Needs an abstract + concrete implementation because we
 * do not want to depend on Servlet3 APIs at runtime, so that
 * Stripes continues to run in Servlet2 containers.
 */
public abstract class AsyncResponse implements Resolution {

	private static final Log log = Log.getInstance(AsyncResponse.class);

	private static final String REQ_ATTR_NAME = "__Stripes_Async_Resolution";
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final ActionBean bean;
	private final Method handler;

	// store static reference to impl constructor
	// in order to avoid useless lookups
	private static Constructor<?> ctor = null;

	private boolean handlerInvoked = false;

	static {
		try {
			HttpServletRequest.class.getMethod("startAsync");
			Class<?> impl = Class.forName("net.sourceforge.stripes.controller.AsyncResponseServlet3");
			ctor = impl.getDeclaredConstructor(
				HttpServletRequest.class,
				HttpServletResponse.class,
				ActionBean.class,
				Method.class);
		} catch (NoSuchMethodException e) {
			// servlet3 not available
			log.info("Container is not using Servlet3 : Async event handlers will throw runtime exceptions.");
		} catch (Exception e) {
			// should not happen unless we break internals (bad refactor etc).
			log.error("Exception while initializing AsyncResponse implementation class.", e);
			throw new RuntimeException(e);
		}

	}

	private Runnable cleanupCallback;

	AsyncResponse(HttpServletRequest request, HttpServletResponse response, ActionBean bean, Method handler) {
		this.request = request;
		this.response = response;
		this.bean = bean;
		this.handler = handler;
		// bind to request so that Resolutions can access
		request.setAttribute(REQ_ATTR_NAME, this);
	}

	/**
	 * Return the AsyncResponse bound to the request, if any.
	 * Primarily used by Resolutions in order to complete processing
	 * accordingly when async is started.
	 * @param request the request
	 * @return the AsyncResponse or null
	 */
	public static AsyncResponse get(HttpServletRequest request) {
		return (AsyncResponse)request.getAttribute(REQ_ATTR_NAME);
	}

	void setCleanupCallback(Runnable cleanupCallback) {
		this.cleanupCallback = cleanupCallback;
	}

	void cleanup() {
		if (cleanupCallback != null) {
			cleanupCallback.run();
		}
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	@Override
	public final void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// invoke the handler (start async has been done already) and let it complete...
		if (handlerInvoked) {
			throw new StripesRuntimeException("Handler already invoked.");
		}
		handlerInvoked = true;
		handler.invoke(bean, this);
	}

	/**
	 * Completes asynchronous processing.
	 */
	public abstract void complete();

	/**
	 * Completes asynchronous processing and executes passed resolution.
	 */
	public abstract void complete(Resolution resolution);

	/**
	 * Dispatches to a webappo resource
	 * @param path the path to dispatch to
	 */
	public abstract void dispatch(String path);

	/**
	 * Return the timeout for async requests
	 * @return the timeout in milliseconds
	 */
	public abstract long getTimeout();

	/**
	 * Set the timeout for async requests
	 * @param timeout the timout in milliseconds
	 */
	public abstract void setTimeout(long timeout);

	static AsyncResponse newInstance(HttpServletRequest request, HttpServletResponse response, ActionBean bean, Method handler) {
		if (ctor == null) {
			throw new StripesRuntimeException("Async events are not available in your container (requires Servlet3+).");
		}
		try {
			Object o = ctor.newInstance(request, response, bean, handler);
			return (AsyncResponse)o;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
