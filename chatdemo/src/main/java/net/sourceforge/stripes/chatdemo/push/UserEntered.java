package net.sourceforge.stripes.chatdemo.push;

public class UserEntered extends PushEvent {

	private final String username;

	public UserEntered(String username) {
		super();
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
}
