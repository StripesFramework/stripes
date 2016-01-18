package net.sourceforge.stripes.action;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AsyncResolution implements Resolution {

	private AsyncContext asyncContext;

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

	private HttpServletRequest request;
	private HttpServletResponse response;

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	@Override
	public final void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		this.request = request;
		this.response = response;
		executeAsync();
	}

	protected abstract void executeAsync() throws Exception;

	protected void dispatch(String path) {
		getAsyncContext().dispatch(path);
	}

	protected void complete() {
		getAsyncContext().complete();
	}

	protected void complete(Resolution resolution) {
		try {
			resolution.execute(getRequest(), getResponse());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


}
