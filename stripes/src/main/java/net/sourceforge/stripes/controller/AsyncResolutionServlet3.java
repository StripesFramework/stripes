package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.Log;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.lang.reflect.Method;

public class AsyncResolutionServlet3 extends AsyncResolution {

	private final static Log log = Log.getInstance(AsyncResolutionServlet3.class);

	private final AsyncContext asyncContext;

	public AsyncResolutionServlet3(HttpServletRequest request,
								   HttpServletResponse response,
								   ActionBean bean,
								   Method handler) {
		super(request, response, bean, handler);
		this.asyncContext = request.startAsync(request, response);
		// remove currentContext ThreadLocal
		ExecutionContext.clearContextThreadLocal();
		// start async processing
		log.debug("Starting async processing from action ", bean);
		// register listener for finalizing the async processing
		asyncContext.addListener(new AsyncListener() {

			private boolean completed = false;

			private void doComplete() {
				if (!completed) {
					completed = true;
					cleanup();
				}
			}

			public void onComplete(AsyncEvent event) throws IOException {
				log.debug("Async context completed=", event.getAsyncContext());
				doComplete();
			}

			// TODO i18n, use stripes exception handlers
			public void onTimeout(AsyncEvent event) throws IOException {
				log.error("Async context timeout after ", event.getAsyncContext().getTimeout(), "ms, ctx=", event.getAsyncContext());
				HttpServletResponse response = (HttpServletResponse) event.getSuppliedResponse();
				response.sendError(500, "Operation timed out");
				doComplete();
			}

			public void onError(AsyncEvent event) throws IOException {
				log.error("Async context error=", event.getAsyncContext());
				HttpServletResponse response = (HttpServletResponse) event.getSuppliedResponse();
				Throwable err = event.getThrowable();
				String msg = err != null ? err.getMessage() : "";
				response.sendError(500, msg);
				doComplete();
			}

			// this one is not called because we register the listener after starting the async context...
			public void onStartAsync(AsyncEvent event) throws IOException {
				log.debug("Async context started=", event.getAsyncContext(),
					"request=", event.getSuppliedRequest(),
					"response=", event.getSuppliedResponse());
			}
		});

	}

	public AsyncContext getAsyncContext() {
		return asyncContext;
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

	@Override
	public void dispatch(String path) {
		getAsyncContext().dispatch(path);
	}

	@Override
	public long getTimeout() {
		return getAsyncContext().getTimeout();
	}

	@Override
	public void setTimeout(long timeout) {
		getAsyncContext().setTimeout(timeout);
	}
}
