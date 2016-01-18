package net.sourceforge.stripes.mock;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;

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
	public void testTimeout() throws Exception {
		MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
		trip.execute("doAsyncTimeout");
		AsyncActionBean bean = trip.getActionBean(AsyncActionBean.class);
		// wait for longer than timeout
		Thread.sleep(3000);
		assertNotNull(bean);
		assertTrue(!bean.isCompleted());
		HttpServletResponse response = bean.getContext().getResponse();
		assertEquals(response.getStatus(), 500);
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
					getResponse().getWriter().write("DONE");
					completed = true;
					complete();
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

		public boolean isCompleted() {
			return completed;
		}
	}


}
