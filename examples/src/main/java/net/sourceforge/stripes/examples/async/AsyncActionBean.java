package net.sourceforge.stripes.examples.async;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.examples.bugzooky.ext.Public;
import net.sourceforge.stripes.validation.Validate;
import org.apache.http.HttpHost;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;

@Public
@UrlBinding("/async")
public class AsyncActionBean implements ActionBean {

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
	public Resolution asyncEvent() {

		// we return an AsyncResolution : this triggers the asynchronous servlet mode...
		return new AsyncResolution() {

			// only this method to implement. you must complete() or dispatch() yourself.
			public void execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

				// we use an Async Http Client in order to call the github web service as a demo.
				// the async http client calls on of the lambdas when he's done, and
				// then we dispatch to a JSP, completing the async request.

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
							dispatchToJsp(getAsyncContext());
						} else {
							ghResponse = result.getStatusLine().getReasonPhrase();
							dispatchToJsp(getAsyncContext());
						}

					})
					.failed(ex -> {

						// http client failure
						clientException = ex;
						dispatchToJsp(getAsyncContext());

					})
					.cancelled(() -> {

						// just for demo, we never call it...
						cancelled = true;
						dispatchToJsp(getAsyncContext());

					})
					.get(); // trigger async request
			}
		};
	}

	@DontValidate
	public Resolution asyncEventThatTimeouts() {
		return new AsyncResolution() {
			public void execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
				getAsyncContext().setTimeout(1000);
				getAsyncContext().getResponse().getWriter().write("OK");
				// never call complete/dispatch...
			}
		};
	}

	@DontValidate
	public Resolution asyncEventThatThrows() {
		return new AsyncResolution() {
			public void execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
				throw new RuntimeException("WTF");
			}
		};
	}

	// helper dispatch method
	private void dispatchToJsp(AsyncContext asyncContext) {
		asyncContext.dispatch("/WEB-INF/async/async.jsp");
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
