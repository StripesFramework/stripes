package net.sourceforge.stripes.controller;

public class AsyncEvent {

	private final AsyncResponse asyncResponse;
	private final Throwable throwable;

	AsyncEvent(AsyncResponse asyncResponse, Throwable throwable) {
		this.asyncResponse = asyncResponse;
		this.throwable = throwable;
	}

	public AsyncResponse getAsyncResponse() {
		return asyncResponse;
	}

	public Throwable getThrowable() {
		return throwable;
	}
}
