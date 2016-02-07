package net.sourceforge.stripes.chatdemo.actions;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.chatdemo.ChatApp;
import net.sourceforge.stripes.controller.AsyncResponse;
import net.sourceforge.stripes.validation.Validate;

import java.util.HashMap;
import java.util.Map;

@UrlBinding("/api/{$event}")
@RestActionBean
public class ChatApiActionBean extends BaseActionBean {

	@Validate(required = true, on = "post")
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	// needed otherwise Stripes doesn't start
	@DontBind
	@DefaultHandler
	public Resolution get() {
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("sucess", true);
		return new JsonResolution(res);
	}

	@DontBind
	@GET
	public void messages(AsyncResponse asyncResponse) {
		asyncResponse.setTimeout(1000 * 60 * 10);
		ChatApp chatApp = ChatApp.get(getContext().getServletContext());
		chatApp.connect(asyncResponse);
	}


	@POST
	public Resolution post() {
		ChatApp chatApp = ChatApp.get(getContext().getServletContext());
		chatApp.post(getContext().getRequest(), message);
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("sucess", true);
		return new JsonResolution(res);
	}

}
