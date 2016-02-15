package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.Log;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Concrete implementation for AsyncResponse in Servlet3 containers.
 * Handles cleanup on completion, and delegates to servlet's AsyncContext methods.
 * This class is loaded via reflection and should not be linked to, for compat reasons.
 */
public class AsyncResponseServlet3 extends AsyncResponse {

	private final static Log log = Log.getInstance(AsyncResponseServlet3.class);

	private final AsyncContext asyncContext;

	public AsyncResponseServlet3(HttpServletRequest request,
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
				notifyListenersComplete();
				doComplete();
			}

			public void onTimeout(AsyncEvent event) throws IOException {
				log.warn("Async context timeout after ", event.getAsyncContext().getTimeout(), "ms, ctx=", event.getAsyncContext());
				HttpServletResponse response = (HttpServletResponse) event.getSuppliedResponse();
				notifyListenersTimeout();
				response.sendError(500, "Operation timed out");
				getAsyncContext().complete();
				doComplete();
			}

			public void onError(AsyncEvent event) throws IOException {
				log.error("Async context error=", event.getAsyncContext());
				Throwable err = event.getThrowable();
				notifyListenersError(err);
				HttpServletResponse response = (HttpServletResponse) event.getSuppliedResponse();
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
		log.debug("Completing AsyncResponse ", this);
		getAsyncContext().complete();
	}

	public void complete(Resolution resolution) {
		log.debug("Completing AsyncResponse ", this, " with Resolution ", resolution);
		try {
			resolution.execute(getRequest(), getResponse());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void dispatch(String path) {
		log.debug("Dispatching AsyncResponse ", this, " to path ", path);

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
