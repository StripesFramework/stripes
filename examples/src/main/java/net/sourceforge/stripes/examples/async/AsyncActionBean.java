package net.sourceforge.stripes.examples.async;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.examples.bugzooky.ext.Public;
import net.sourceforge.stripes.validation.Validate;
import org.apache.http.HttpHost;

import javax.servlet.AsyncContext;
import java.io.ByteArrayOutputStream;

@Public
@UrlBinding("/async")
public class AsyncActionBean implements ActionBean {

	private static final String JSP_PATH = "/WEB-INF/async/async.jsp";
	private ActionBeanContext context;
	public ActionBeanContext getContext() {
		return context;
	}
	public void setContext(ActionBeanContext context) {
		this.context = context;
	}

	// property to test binding/validation
	@Validate(required = true)
	private String someProp;

	// those are set by the http client response, and
	// used in the JSP...
	private Exception clientException;
	private boolean cancelled;
	private int status;
	private String ghResponse;

	// display the test page
	@DefaultHandler
	@DontValidate
	public Resolution display() {
		return new ForwardResolution("/WEB-INF/async/async.jsp");
	}

	/**
	 * asynchronously fetch data from a remote web service (github)
	 * and set instance fields for use in the view.
 	 */
	public void asyncEvent(AsyncResolution async) {

		// we use an Async Http Client in order to call the github web service as a demo.
		// the async http client calls back one of the lambdas when it's done, and
		// then we complete the async request.

		final Resolution forwardResolution = new ForwardResolution(JSP_PATH);
		HttpHost host = new HttpHost("api.github.com", 443, "https");
		new AsyncHttpClient(host)
			.buildRequest("/repos/StripesFramework/stripes/commits")
			.completed(result -> {

				// response is returned, deserialize result
				status = result.getStatusLine().getStatusCode();
				if (status == 200) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					try {
						result.getEntity().writeTo(bos);
						bos.close();
						ghResponse = bos.toString("UTF-8");
					} catch (Exception e) {
						clientException = e;
					}
					async.complete(forwardResolution);
				} else {
					ghResponse = result.getStatusLine().getReasonPhrase();
					async.complete(forwardResolution);
				}

			})
			.failed(ex -> {

				// http client failure
				clientException = ex;
				async.complete(forwardResolution);

			})
			.cancelled(() -> {

				// just for demo, we never call it...
				cancelled = true;
				async.complete(forwardResolution);

			})
			.get(); // trigger async request
	}

	@DontValidate
	public void asyncEventThatTimeouts(AsyncResolution r) throws Exception {
		r.getAsyncContext().setTimeout(1000);
		r.getResponse().getWriter().write("OK");
		// never call complete/dispatch...
	}

	@DontValidate
	public void asyncEventThatThrows(AsyncResolution r) {
		throw new RuntimeException("BOOM");
	}

	// getters for instance fields that have been set by event method

	public boolean isCancelled() {
		return cancelled;
	}

	public int getStatus() {
		return status;
	}

	public String getGhResponse() {
		return ghResponse;
	}

	public Exception getClientException() {
		return clientException;
	}

	// get/set for test binding prop

	public String getSomeProp() {
		return someProp;
	}

	public void setSomeProp(String someProp) {
		this.someProp = someProp;
	}

}
