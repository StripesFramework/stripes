package net.sourceforge.stripes.chatdemo.push;

public class MessagePosted extends PushEvent {

	private final String username;
	private final String message;

	public MessagePosted(String username, String message) {
		super();
		this.username = username;
		this.message = message;
	}

	public String getUsername() {
		return username;
	}

	public String getMessage() {
		return message;
	}
}
