package net.sourceforge.stripes.action;

import javax.servlet.AsyncContext;

public abstract class AsyncResolution implements Resolution {

	private AsyncContext asyncContext;

	public AsyncContext getAsyncContext() {
		return asyncContext;
	}

	public void setAsyncContext(AsyncContext asyncContext) {
		this.asyncContext = asyncContext;
	}

}
