package net.sourceforge.stripes.mock;

import net.sourceforge.stripes.util.Log;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockAsyncContext implements AsyncContext {

	private static final Log log = Log.getInstance(MockAsyncContext.class);

	private final ServletRequest request;
	private final ServletResponse response;
	private final List<AsyncListener> listeners = new ArrayList<AsyncListener>();

	private boolean completed = false;
	private boolean timedOut = false;
	private long timeout = 10000;
	private long startedOn;

	public MockAsyncContext(ServletRequest request, ServletResponse response) {
		this.startedOn = System.currentTimeMillis();
		this.request = request;
		this.response = response;
		log.info("async started, request=", request, ", response=", response);
	}

	@Override
	public ServletRequest getRequest() {
		return request;
	}

	@Override
	public ServletResponse getResponse() {
		return response;
	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		return request != null && response != null;
	}

	private void checkNotCompleted() {
		if (completed) {
			throw new IllegalStateException("already dispatched or completed !");
		}
	}

	@Override
	public void dispatch() {
		complete();
	}

	@Override
	public void dispatch(String path) {
		dispatch();
	}

	@Override
	public void dispatch(ServletContext context, String path) {
		dispatch();
	}

	@Override
	public void complete() {
		checkNotCompleted();
		completed = true;
		AsyncEvent evt = new AsyncEvent(this, request, response);
		for (AsyncListener l : listeners) {
			try {
				l.onComplete(evt);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void start(Runnable run) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void addListener(AsyncListener listener) {
		listeners.add(listener);
	}

	@Override
	public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
		listeners.add(listener);
	}

	@Override
	public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	public void waitForCompletion() throws Exception {
		log.debug("Waiting for completion...");
		while(true) {
			long elapsed = System.currentTimeMillis() - startedOn;
			if (elapsed > timeout) {
				timedOut = true;
				// invoke listeners timeout
				AsyncEvent timeoutEvent = new AsyncEvent(MockAsyncContext.this, request, response);
				for (AsyncListener l : listeners) {
					try {
						l.onTimeout(timeoutEvent);
					} catch (Exception e) {
						log.warn("listener onTimeout threw exception", e);
					}
				}
				log.error("Operation timed out, will throw exception");
				throw new RuntimeException("Operation timed out (elapsed=" + elapsed + ", timeout=" + timeout + ")");
			} else if (completed) {
				log.debug("...Completed in ", elapsed, "ms");
				break;
			}
			Thread.sleep(200);
		}
	}

	public long getStartedOn() {
		return startedOn;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public boolean isCompleted() {
		return completed;
	}
}
