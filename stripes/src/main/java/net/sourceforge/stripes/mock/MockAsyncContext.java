package net.sourceforge.stripes.mock;

import net.sourceforge.stripes.util.Log;

import jakarta.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rgras_000
 */
public class MockAsyncContext implements AsyncContext {

    private static final Log log = Log.getInstance(MockAsyncContext.class);

    private final ServletRequest request;
    private final ServletResponse response;
    private final List<AsyncListener> listeners = new ArrayList<AsyncListener>();

    private boolean completed = false;
    private boolean timedOut = false;
    private long timeout = 10000;
    private long startedOn;

    /**
     *
     * @param request
     * @param response
     */
    public MockAsyncContext(ServletRequest request, ServletResponse response) {
        this.startedOn = System.currentTimeMillis();
        this.request = request;
        this.response = response;
        log.info("async started, request=", request, ", response=", response);
    }

    /**
     *
     * @return
     */
    @Override
    public ServletRequest getRequest() {
        return request;
    }

    /**
     *
     * @return
     */
    @Override
    public ServletResponse getResponse() {
        return response;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasOriginalRequestAndResponse() {
        return request != null && response != null;
    }

    private void checkNotCompleted() {
        if (completed) {
            throw new IllegalStateException("already dispatched or completed !");
        }
    }

    /**
     *
     */
    @Override
    public void dispatch() {
        complete();
    }

    /**
     *
     * @param path
     */
    @Override
    public void dispatch(String path) {
        dispatch();
    }

    /**
     *
     * @param context
     * @param path
     */
    @Override
    public void dispatch(ServletContext context, String path) {
        dispatch();
    }

    /**
     *
     */
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

    /**
     *
     * @param run
     */
    @Override
    public void start(Runnable run) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     *
     * @param listener
     */
    @Override
    public void addListener(AsyncListener listener) {
        listeners.add(listener);
    }

    /**
     *
     * @param listener
     * @param servletRequest
     * @param servletResponse
     */
    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
        listeners.add(listener);
    }

    /**
     *
     * @param <T>
     * @param clazz
     * @return
     * @throws ServletException
     */
    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     *
     * @param timeout
     */
    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     *
     * @return
     */
    @Override
    public long getTimeout() {
        return timeout;
    }

    /**
     *
     * @throws Exception
     */
    public void waitForCompletion() throws Exception {
        log.debug("Waiting for completion...");
        while (true) {
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

    /**
     *
     * @return
     */
    public long getStartedOn() {
        return startedOn;
    }

    /**
     *
     * @return
     */
    public boolean isTimedOut() {
        return timedOut;
    }

    /**
     *
     * @return
     */
    public boolean isCompleted() {
        return completed;
    }
}
