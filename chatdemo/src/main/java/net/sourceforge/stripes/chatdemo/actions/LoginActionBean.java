package net.sourceforge.stripes.chatdemo.actions;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.chatdemo.ChatApp;
import net.sourceforge.stripes.chatdemo.ChatInterceptor;
import net.sourceforge.stripes.validation.Validate;

@UrlBinding("/login")
public class LoginActionBean extends BaseActionBean {

	@Validate(required = true, minlength = 4, maxlength = 20, on = "login")
	private String username;

	@DontBind
	@DefaultHandler
	public Resolution display() {
		return new ForwardResolution("/WEB-INF/login.jsp");
	}

	public Resolution login() {
		ActionBeanContext context = getContext();
		ChatInterceptor.login(context.getRequest(), username);
		return new RedirectResolution(ChatActionBean.class);
	}

	public Resolution logout() {
		ChatApp.get(getContext().getServletContext()).disconnect(getContext().getRequest());
		ChatInterceptor.logout(getContext().getRequest());
		return new RedirectResolution(getClass());
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
