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
import java.util.ArrayList;
import java.util.List;

/**
 * Used by asynchrounous event handlers.
 * Instances of this class are passed by Stripes to asynchronous event handlers,
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

	private final List<AsyncListener> listeners = new ArrayList<AsyncListener>();

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

	/**
	 * Return the request associated to asychronous processing.
	 * @return the http request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * Return the response associated to asynchronous processing.
	 * @return the http response
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * Called by Stripes internally in order to execute the asynchonous event.
	 * You should neved need to invoke this method yourself.
	 * @param request the current HttpServletRequest
	 * @param response the current HttpServletResponse
	 * @throws Exception if the event handler throws an Exception
	 */
	@Override
	public final void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// invoke the handler (start async has been done already) and let it complete...
		if (handlerInvoked) {
			throw new StripesRuntimeException("Handler already invoked.");
		}
		handlerInvoked = true;
		log.debug("Invoking async event ", handler.getName(), " on bean ", bean);
		handler.invoke(bean, this);
	}

	/**
	 * Adds a listener to this async response. Listeners can be used to
	 * be notified of async lifecycle events.
	 * @param listener the listener to add.
	 */
	public void addListener(AsyncListener listener) {
		listeners.add(listener);
	}

	void notifyListenersComplete() {
		AsyncEvent event = new AsyncEvent(this, null);
		for (AsyncListener listener : listeners) {
			try {
				listener.onComplete(event);
			} catch (Exception e) {
				log.error("Error notifying listener " + listener, e);
			}
		}
	}

//	void notifyListenersStartAsync() {
//		AsyncEvent event = new AsyncEvent(this, null);
//		for (AsyncListener listener : listeners) {
//			try {
//				listener.onStartAsync(event);
//			} catch (Exception e) {
//				log.error("Error notifying listener " + listener, e);
//			}
//		}
//	}

	void notifyListenersError(Throwable error) {
		AsyncEvent event = new AsyncEvent(this, error);
		for (AsyncListener listener : listeners) {
			try {
				listener.onError(event);
			} catch (Exception e) {
				log.error("Error notifying listener " + listener, e);
			}
		}
	}

	void notifyListenersTimeout() {
		AsyncEvent event = new AsyncEvent(this, null);
		for (AsyncListener listener : listeners) {
			try {
				listener.onTimeout(event);
			} catch (Exception e) {
				log.error("Error notifying listener " + listener, e);
			}
		}
	}

	/**
	 * Completes asynchronous processing.
	 */
	public abstract void complete();

	/**
	 * Executes passed resolution, and completes asynchronous processing.
	 */
	public abstract void complete(Resolution resolution);

	/**
	 * Dispatches to a web application resource
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
