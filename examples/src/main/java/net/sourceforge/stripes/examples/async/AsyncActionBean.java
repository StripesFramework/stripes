package net.sourceforge.stripes.examples.async;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.AsyncResponse;
import net.sourceforge.stripes.examples.bugzooky.ext.Public;
import net.sourceforge.stripes.validation.Validate;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

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
	public void asyncEvent(final AsyncResponse async) {

		// we use an Async Http Client in order to call the github web service as a demo.
		// the async http client calls back one of the lambdas when it's done, and
		// then we complete the async request.

		final Resolution forwardResolution = new ForwardResolution(JSP_PATH);
		HttpHost host = new HttpHost("api.github.com", 443, "https");
		new AsyncHttpClient(host)
			.buildRequest("/repos/StripesFramework/stripes/commits")
			.completed(new AsyncHttpClient.Consumer<HttpResponse>() {
				@Override
				public void accept(HttpResponse result) {
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
				}
			})
			.failed(new AsyncHttpClient.Consumer<Exception>() {
				@Override
				public void accept(Exception e) {
					// http client failure
					clientException = e;
					async.complete(forwardResolution);
				}
			})
			.cancelled(new Runnable() {
				@Override
				public void run() {
					// just for demo, we never call it...
					cancelled = true;
					async.complete(forwardResolution);

				}
			}).get(); // trigger async request
	}

	@DontValidate
	public void asyncEventThatTimeouts(AsyncResponse r) throws Exception {
		r.setTimeout(1000);
		r.getResponse().getWriter().write("OK");
		// never call complete/dispatch...
	}

	@DontValidate
	public void asyncEventThatThrows(AsyncResponse r) {
		throw new RuntimeException("BOOM");
	}

	@DontValidate
	public void asyncWrites(final AsyncResponse r) {
		final String[] parts = new String[]{
			"This", "is", "asynchronously", "written", "!",
			"We", "use", "readystatechange", "in",
			"order", "to", "be", "notified", "when", "the",
			"server", "pushes", "some", "data"
		};
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean error = false;
				try {
					PrintWriter w = r.getResponse().getWriter();
					for (int i = 0; i < parts.length; i++) {
						w.println("<div class=\"asyncWrite\">" + parts[i] + "</div>");
						w.flush();
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// don't care
						}
					}
				} catch (IOException e) {
					error = true;
					r.complete(new ErrorResolution(500, e.getMessage()));
				}
				if (!error) {
					r.complete(new StreamingResolution("text/plain", "<em>Bye</em>"));
				}
			}
		}).start();
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
