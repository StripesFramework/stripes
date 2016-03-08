package net.sourceforge.stripes.mock;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.*;
import static org.testng.Assert.*;

import net.sourceforge.stripes.controller.AsyncEvent;
import net.sourceforge.stripes.controller.AsyncListener;
import net.sourceforge.stripes.controller.AsyncResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestMockAsync extends FilterEnabledTestBase {

	private AsyncActionBean execute(String eventName) throws Exception {
		System.out.println("==> Executing : " + eventName);
		MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
		trip.execute(eventName);
		AsyncActionBean bean = trip.getActionBean(AsyncActionBean.class);
		assertNotNull(bean);
		assertEquals(eventName, bean.getContext().getEventName());
		assertTrue(bean.completed);
		assertTrue(trip.getRequest().getAsyncContext().isCompleted());
		System.out.println("==> done with : " + eventName);
		return bean;
	}
	
	@Test
	public void testSuccess() throws Exception {
		AsyncActionBean bean = execute("doAsync");
		assertNotNull(bean);
		assertTrue(bean.isCompleted());
	}

	@Test
	public void testReallyAsync() throws Exception {
		AsyncActionBean bean = execute("doReallyAsync");
		assertNotNull(bean);
		assertTrue(bean.isCompleted());
	}

	@Test
	public void testTimeout() throws Exception {
		MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
		boolean caught = false;
		try {
			trip.execute("doAsyncTimeout");
		} catch(Exception e) {
			caught = true;
			e.printStackTrace();
		}
		assertTrue(caught);
		AsyncActionBean bean = trip.getActionBean(AsyncActionBean.class);
		assertNotNull(bean);
		assertTrue(!bean.isCompleted());
		HttpServletResponse response = bean.getContext().getResponse();
		assertEquals(response.getStatus(), 500);
		assertTrue(trip.getRequest().getAsyncContext().isCompleted());
	}

	@Test
	public void testRegularException() throws Exception {
		boolean caught = false;
		try {
			MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
			trip.execute("doRegularException");
		} catch(Exception e) {
			e.printStackTrace();
			caught = true;
		}
		assertTrue(caught);
	}

	private void assertThrows(String event) {
		boolean caught = false;
		try {
			MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
			trip.execute(event);
			assertTrue(trip.getRequest().getAsyncContext().isCompleted());
		} catch(Exception e) {
			e.printStackTrace();
			caught = true;
		}
		assertTrue(caught);
	}


	@Test
	public void testAsyncException() throws Exception {
		assertThrows("doAsyncException");
	}

	@Test
	public void testCompleteWithForwardResolution() throws Exception {
		execute("doAsyncAndCompleteWithForwardResolution");
	}

	@Test
	public void doAsyncInThreadAndCompleteWithForwardResolution() throws Exception {
		execute("doAsyncInThreadAndCompleteWithForwardResolution");
	}

	@Test
	public void doAsyncInThreadWithListener() throws Exception {
		execute("doAsyncInThreadWithListener");
	}

	@Test
	public void doAsyncWithResolutionThatThrows() throws Exception {
		assertThrows("doAsyncWithResolutionThatThrows");
	}

	@Test
	public void doAsyncWithForwardToNowhere() throws Exception {
		MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
		trip.execute("doAsyncWithForwardToNowhere");
		AsyncActionBean bean = trip.getActionBean(AsyncActionBean.class);
		assertNotNull(bean);
		assertEquals("doAsyncWithForwardToNowhere", bean.getContext().getEventName());
		assertFalse(bean.completed);
		assertTrue(trip.getRequest().getAsyncContext().isCompleted());
	}

	@UrlBinding("/async")
	public static class AsyncActionBean implements ActionBean {

		private boolean completed = false;
		private ActionBeanContext context;

		public ActionBeanContext getContext() {
			return context;
		}

		public void setContext(ActionBeanContext context) {
			this.context = context;
		}

		@DefaultHandler
		public void doAsync(AsyncResponse r) throws Exception {
			System.out.println("Not Really Async...");
			r.getResponse().getWriter().write("DONE");
			completed = true;
			r.complete();
		}

		public void doReallyAsync(final AsyncResponse r) throws Exception {
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println("Really Async !");
					try {
						r.getResponse().getWriter().write("DONE");
						completed = true;
						r.complete();
					} catch (IOException e) {
						e.printStackTrace(); // we let it timeout...
					}
				}
			}).start();
		}

		public void doAsyncTimeout(AsyncResponse r) {
			r.setTimeout(1000);
			// we never complete !
		}

		public Resolution doRegularException() {
			throw new RuntimeException("boom");
		}

		public void doAsyncException(AsyncResponse r) {
			throw new RuntimeException("Async boom");
		}

		public void doAsyncAndCompleteWithForwardResolution(AsyncResponse r) {
			System.out.println("hiya, I'm forwarding...");
			completed = true;
			r.complete(new ForwardResolution("/foo/bar.jsp"));
		}

		public void doAsyncInThreadAndCompleteWithForwardResolution(final AsyncResponse response) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println("hiya, I'm inside a separate thread");
					completed = true;
					System.out.println("Completing");
					response.complete(new ForwardResolution("/foo/bar.jsp"));
				}
			}).start();
		}

		public void doAsyncInThreadWithListener(final AsyncResponse response) {
			// set the completed flag with a listener
			response.addListener(new AsyncListener() {
				@Override
				public void onComplete(AsyncEvent event) {
					completed = true;
				}

				@Override
				public void onError(AsyncEvent event) {
				}

				@Override
				public void onTimeout(AsyncEvent event) {
				}
			});
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						for (int i = 0 ; i < 10 ; i++) {
							response.getResponse().getWriter().write("i=" + i);
							Thread.sleep(100);
						}
						System.out.println("hiya, I'm inside a separate thread and I use listeners");
					} catch (Exception e) {
						e.printStackTrace();
					}
					response.complete();
				}
			}).start();
		}

		public void doAsyncWithResolutionThatThrows(final AsyncResponse asyncResponse) {
			asyncResponse.complete(new ResolutionThatThrows());
		}

		public void doAsyncWithForwardToNowhere(final AsyncResponse asyncResponse) {
			asyncResponse.complete(new ForwardResolution("/i/dont/exist"));
		}

		public boolean isCompleted() {
			return completed;
		}

	}

	private static class ResolutionThatThrows implements Resolution {
		@Override
		public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
			throw new RuntimeException("I throw exceptions only");
		}
	}


}

