package net.sourceforge.stripes.chatdemo.actions;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.chatdemo.ChatInterceptor;

@UrlBinding("/chat")
public class ChatActionBean extends BaseActionBean {

	@DefaultHandler
	public Resolution display() {
		return new ForwardResolution("/WEB-INF/chat.jsp");
	}

	public String getUsername() {
		return ChatInterceptor.getUsername(getContext().getRequest());
	}

}
