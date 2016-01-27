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

public abstract class AsyncResolution implements Resolution {

	private static final Log log = Log.getInstance(AsyncResolution.class);

	private static final String REQ_ATTR_NAME = "__Stripes_Async_Resolution";
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final ActionBean bean;
	private final Method handler;

	private static Constructor<?> ctor = null;

	static {
		try {
			HttpServletRequest.class.getMethod("startAsync");
			Class<?> impl = Class.forName("net.sourceforge.stripes.controller.AsyncResolutionServlet3");
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
			log.error("Exception while initializing AsyncResolution implementation class.", e);
			throw new RuntimeException(e);
		}

	}

	private Runnable cleanupCallback;
	private long timeout;

	protected AsyncResolution(HttpServletRequest request, HttpServletResponse response, ActionBean bean, Method handler) {
		this.request = request;
		this.response = response;
		this.bean = bean;
		this.handler = handler;
		request.setAttribute(REQ_ATTR_NAME, this);
	}

	public static AsyncResolution get(HttpServletRequest request) {
		return (AsyncResolution)request.getAttribute(REQ_ATTR_NAME);
	}

	public void setCleanupCallback(Runnable cleanupCallback) {
		this.cleanupCallback = cleanupCallback;
	}

	public void cleanup() {
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
	public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// invoke the handler (start async has been done already) and let it complete...
		handler.invoke(bean, this);
	}

	public abstract void complete();

	public abstract void complete(Resolution resolution);

	public abstract void dispatch(String path);

	public abstract long getTimeout();

	public abstract void setTimeout(long timeout);

	public static AsyncResolution newInstance(HttpServletRequest request, HttpServletResponse response, ActionBean bean, Method handler) {
		if (ctor == null) {
			throw new StripesRuntimeException("Async events are not available in your container (requires Servlet3+).");
		}
		try {
			Object o = ctor.newInstance(request, response, bean, handler);
			return (AsyncResolution)o;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
