package net.sourceforge.stripes.mock;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.*;
import static org.testng.Assert.*;

import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TestMockAsync extends FilterEnabledTestBase {

	@Test
	public void testSuccess() throws Exception {
		MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
		trip.execute();
		AsyncActionBean bean = trip.getActionBean(AsyncActionBean.class);
		assertNotNull(bean);
		assertTrue(bean.isCompleted());
	}

	@Test
	public void testReallyAsync() throws Exception {
		MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
		trip.execute("doReallyAsync");
		AsyncActionBean bean = trip.getActionBean(AsyncActionBean.class);
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
			caught = true;
		}
		assertTrue(caught);
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
		public Resolution doAsync() {
			return new AsyncResolution() {
				@Override
				protected void executeAsync() throws Exception {
					Thread.sleep(5000);
					System.out.println("Not Really Async...");
					getResponse().getWriter().write("DONE");
					completed = true;
					complete();
				}
			};
		}

		public Resolution doReallyAsync() {
			return new AsyncResolution() {
				@Override
				protected void executeAsync() throws Exception {
					new Thread(() -> {
						System.out.println("Really Async !");
						try {
							getResponse().getWriter().write("DONE");
							completed = true;
							complete();
						} catch (IOException e) {
							// will timeout...
							e.printStackTrace();
						}
					}).start();
				}
			};
		}

		public Resolution doAsyncTimeout() {
			return new AsyncResolution() {
				@Override
				protected void executeAsync() throws Exception {
					getAsyncContext().setTimeout(1000);
					// we never complete !
				}
			};
		}

		public Resolution doRegularException() {
			throw new RuntimeException("boom");
		}

		public Resolution doAsyncException() {
			return new AsyncResolution() {
				@Override
				protected void executeAsync() throws Exception {
					throw new RuntimeException("Async boom");
				}
			};
		}

		public boolean isCompleted() {
			return completed;
		}
	}


}
