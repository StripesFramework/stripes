package net.sourceforge.stripes.controller;

/**
 * Instances of this class are passed by Stripes to
 * {@link AsyncListener} when asynchronous events occur.
 */
public final class AsyncEvent {

	private final AsyncResponse asyncResponse;
	private final Throwable throwable;

	AsyncEvent(AsyncResponse asyncResponse, Throwable throwable) {
		this.asyncResponse = asyncResponse;
		this.throwable = throwable;
	}

	/**
	 * Return the <code>AsyncResponse</code> associated to the event
	 * @return the async response
	 */
	public AsyncResponse getAsyncResponse() {
		return asyncResponse;
	}

	/**
	 * Return the <code>Throwable</code> associated to the event, if any
	 * @return the throwable
	 */
	public Throwable getThrowable() {
		return throwable;
	}
}
