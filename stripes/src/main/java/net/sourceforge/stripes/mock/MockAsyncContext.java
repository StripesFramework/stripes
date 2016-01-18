package net.sourceforge.stripes.mock;

import net.sourceforge.stripes.util.Log;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class MockAsyncContext implements AsyncContext {

	private static final Log log = Log.getInstance(MockAsyncContext.class);

	private final ServletRequest request;
	private final ServletResponse response;
	private final List<AsyncListener> listeners = new ArrayList<>();


	private boolean completed = false;
	private long timeout = 30000;

	public MockAsyncContext(ServletRequest request, ServletResponse response, ExecutorService executorService) {
		this.request = request;
		this.response = response;
		long startTime = System.currentTimeMillis();
		log.info("async started, request=", request, ", response=", response);
		// trigger the timeout thread...
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						long now = System.currentTimeMillis();
						long elapsed = now - startTime;
						log.debug("timeout thread check : elapsed=", elapsed);
						if (elapsed > timeout) {
							// invoke listeners timeout
							AsyncEvent timeoutEvent = new AsyncEvent(MockAsyncContext.this, request, response);
							for (AsyncListener l : listeners) {
								try {
									l.onTimeout(timeoutEvent);
								} catch (Exception e) {
									log.warn("listener onTimeout threw exception", e);
								}
							}
							break;
						}
						Thread.sleep(200);
					}
				}catch(InterruptedException e){
					// exit the loop
					log.warn("Async Context was Interrupted", e);
				}
			}
		});
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
}
