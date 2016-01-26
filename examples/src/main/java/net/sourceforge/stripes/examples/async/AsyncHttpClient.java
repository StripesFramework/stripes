package net.sourceforge.stripes.examples.async;

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
import org.apache.http.nio.reactor.ConnectingIOReactor;

import java.io.IOException;

/**
 * Wrapper for non-blocking http client example. Avoids cluttering the action bean's code...
 */
public class AsyncHttpClient  {

	private final CloseableHttpAsyncClient asyncClient;
	private final HttpHost host;

	public AsyncHttpClient(HttpHost host) {
		this.host = host;
		try {
			RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(10000)
				.setConnectTimeout(10000).build();
			ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
			PoolingNHttpClientConnectionManager cm =
				new PoolingNHttpClientConnectionManager(ioReactor);
			asyncClient = HttpAsyncClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(cm)
				.build();
			asyncClient.start();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void close() {
		try {
			asyncClient.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public AsyncRequest buildRequest(String uri) {
		return new AsyncRequest(uri);
	}

	public interface Consumer<T> {
		void accept(T t);
	}

	public class AsyncRequest {

		private final String uri;

		private Consumer<HttpResponse> onCompleted;
		private Consumer<Exception> onFailed;
		private Runnable onCancelled;

		private AsyncRequest(String uri) {
			this.uri = uri;
		}

		public AsyncRequest completed(Consumer<HttpResponse> c) {
			onCompleted = c;
			return this;
		}

		public AsyncRequest failed(Consumer<Exception> e) {
			onFailed = e;
			return this;
		}

		public AsyncRequest cancelled(Runnable r) {
			onCancelled = r;
			return this;
		}

		public void get() {
			HttpRequest r = new BasicHttpRequest("GET", uri);
			asyncClient.execute(host, r, new FutureCallback<HttpResponse>() {
				public void completed(HttpResponse result) {
					if (onCompleted != null) {
						onCompleted.accept(result);
					}
					close();
				}

				public void failed(Exception ex) {
					if (onFailed != null) {
						onFailed.accept(ex);
					}
					close();
				}

				public void cancelled() {
					if (onCancelled != null) {
						onCancelled.run();
					}
					close();
				}
			});
		}
	}

}
