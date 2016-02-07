package net.sourceforge.stripes.chatdemo;

import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.chatdemo.actions.LoginActionBean;
import net.sourceforge.stripes.controller.*;

import javax.servlet.http.HttpServletRequest;

@Intercepts({LifecycleStage.BindingAndValidation})
public class ChatInterceptor implements Interceptor {

	private static final String CHAT_USERNAME = "__Chat_Username";

	@Override
	public Resolution intercept(ExecutionContext context) throws Exception {
		if (context.getActionBean() instanceof LoginActionBean) {
			return context.proceed();
		}
		// check wether user has logged in or not...
		String username = (String)context
			.getActionBeanContext()
			.getRequest()
			.getSession()
			.getAttribute(CHAT_USERNAME);
		if (username == null) {
			return new ForwardResolution("/WEB-INF/login.jsp");
		} else {
			return context.proceed();
		}
	}

	public static void login(HttpServletRequest request, String username) {
		request.getSession().setAttribute(CHAT_USERNAME, username);
	}

	public static void logout(HttpServletRequest request) {
		request.getSession().removeAttribute(CHAT_USERNAME);
	}

	public static String getUsername(HttpServletRequest request) {
		return (String)request.getSession().getAttribute(CHAT_USERNAME);
	}
}
