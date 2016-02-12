package net.sourceforge.stripes.controller;

/**
 * Listeners that are notified of asynchronous lifecycle events.
 * Wraps serlvet3's <code>javax.servlet.AsyncListener</code>.
 */
public interface AsyncListener {

	/**
	 * Invoked when asynchronous event is completed.
	 * @param event the event
	 */
	void onComplete(AsyncEvent event);

	/**
	 * Invoked when an error occured during asynchronous processing
	 * @param event the event
	 */
	void onError(AsyncEvent event);

//	/**
//	 * Invoked when asynchronous processing is started
//	 * @param event the event
//	 */
//	void onStartAsync(AsyncEvent event);

	/**
	 * Invoked when asynchronous processing times-out.
	 * @param event the event
	 */
	void onTimeout(AsyncEvent event);

}
