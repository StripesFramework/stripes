package net.sourceforge.stripes.controller;

public interface AsyncListener {

	void onComplete(AsyncEvent event);

	void onError(AsyncEvent event);

	void onStartAsync(AsyncEvent event);

	void onTimeout(AsyncEvent event);

}
