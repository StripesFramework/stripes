package net.sourceforge.stripes.chatdemo.push;

public class UserLeft extends PushEvent {

	private final String username;

	public UserLeft(String username) {
		super();
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
}
