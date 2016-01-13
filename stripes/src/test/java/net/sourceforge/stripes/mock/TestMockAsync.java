package net.sourceforge.stripes.mock;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestMockAsync extends FilterEnabledTestBase {

	@Test(groups="fast")
	public void testDefaultEvent() throws Exception {
		MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), AsyncActionBean.class);
		trip.execute();

		AsyncActionBean bean = trip.getActionBean(AsyncActionBean.class);
		Assert.assertNotNull(bean);
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
				public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
					Thread.sleep(5000);
					AsyncContext asyncContext = getAsyncContext();
					asyncContext.getResponse().getWriter().write("DONE");
					asyncContext.complete();
				}
			};
		}


	}


}
