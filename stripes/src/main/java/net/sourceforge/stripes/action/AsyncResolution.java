package net.sourceforge.stripes.action;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class AsyncResolution implements Resolution {

	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final Object bean;
	private final Method handler;
	private AsyncContext asyncContext;

	public AsyncResolution(HttpServletRequest request, HttpServletResponse response, Object bean, Method handler) {
		this.request = request;
		this.response = response;
		this.bean = bean;
		this.handler = handler;
	}

	public AsyncContext getAsyncContext() {
		return asyncContext;
	}

	public void setAsyncContext(AsyncContext asyncContext) {
		this.asyncContext = asyncContext;
	}

	private ActionBeanContext context;

	public ActionBeanContext getContext() {
		return context;
	}

	public void setContext(ActionBeanContext context) {
		this.context = context;
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

	public void dispatch(String path) {
		getAsyncContext().dispatch(path);
	}

	public void complete() {
		getAsyncContext().complete();
	}

	public void complete(Resolution resolution) {
		try {
			resolution.execute(getRequest(), getResponse());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
