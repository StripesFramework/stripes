package net.sourceforge.stripes.mock;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.*;
import static org.testng.Assert.*;

import net.sourceforge.stripes.controller.AsyncResponse;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TestMockAsync extends FilterEnabledTestBase {

	private AsyncActionBean execute(String eventName) throws Exception {
		MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
		trip.execute(eventName);
		AsyncActionBean bean = trip.getActionBean(AsyncActionBean.class);
		assertNotNull(bean);
		assertEquals(eventName, bean.getContext().getEventName());
		assertTrue(bean.completed);
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

	@Test
	public void testAsyncException() throws Exception {
		boolean caught = false;
		try {
			MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
			trip.execute("doAsyncException");
		} catch(Exception e) {
			e.printStackTrace();
			caught = true;
		}
		assertTrue(caught);
	}

	@Test
	public void testCompleteWithForwardResolution() throws Exception {
		execute("doAsyncAndCompleteWithForwardResolution");
	}

	@Test
	public void testAsyncClassy() throws Exception {
		execute("doAsyncClassy");
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
						// will timeout...
						e.printStackTrace();
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

		public void doAsyncClassy(final AsyncResponse callback) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					completed = true;
					callback.complete(new ForwardResolution("/foo/bar"));
				}
			}).start();
		}

		public boolean isCompleted() {
			return completed;
		}

	}


}

