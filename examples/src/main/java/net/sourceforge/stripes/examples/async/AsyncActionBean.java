package net.sourceforge.stripes.examples.async;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.examples.bugzooky.ext.Public;
import net.sourceforge.stripes.util.Base64;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.ConnectingIOReactor;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;

@Public
@UrlBinding("/async")
public class AsyncActionBean implements ActionBean {

	private ActionBeanContext context;

	private Exception clientException;
	private boolean cancelled;
	private int status;
	private String ghResponse;

	public ActionBeanContext getContext() {
		return context;
	}

	public void setContext(ActionBeanContext context) {
		this.context = context;
	}

	@DefaultHandler
	public Resolution display() {
		return new ForwardResolution("/WEB-INF/async/async.jsp");
	}

	public Resolution asyncEvent() {
		return new AsyncResolution() {
			public void execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

				final CloseableHttpAsyncClient asyncClient = createAsyncHttpClient();
				HttpHost h = new HttpHost("api.github.com", 443, "https");
				HttpRequest r = new BasicHttpRequest("GET", "/repos/StripesFramework/stripes/commits");
				asyncClient.execute(h, r, new FutureCallback<HttpResponse>() {
					public void completed(HttpResponse result) {
						// deserialize result
						status = result.getStatusLine().getStatusCode();
						if (status == 200) {
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							try {
								result.getEntity().writeTo(bos);
								bos.close();
								ghResponse = bos.toString("UTF-8");
								dispatch(asyncClient, getAsyncContext());
							} catch (Exception e) {
								failed(e);
							}
						} else {
							ghResponse = result.getStatusLine().getReasonPhrase();
							dispatch(asyncClient, getAsyncContext());
						}
					}

					public void failed(Exception ex) {
						// ouch !
						clientException = ex;
						dispatch(asyncClient, getAsyncContext());
					}

					public void cancelled() {
						cancelled = true;
						dispatch(asyncClient, getAsyncContext());
					}
				});
			}
		};
	}

	private void dispatch(CloseableHttpAsyncClient asyncClient, AsyncContext c) {
		try {
			asyncClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		c.dispatch("/WEB-INF/async/async.jsp");
	}

	protected CloseableHttpAsyncClient createAsyncHttpClient() {
		try {
			RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(10000)
				.setConnectTimeout(10000).build();
			ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
			PoolingNHttpClientConnectionManager cm =
				new PoolingNHttpClientConnectionManager(ioReactor);
			CloseableHttpAsyncClient res = HttpAsyncClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(cm)
				.build();
			res.start();
			return res;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public int getStatus() {
		return status;
	}

	public String getGhResponse() {
		return ghResponse;
	}
}
