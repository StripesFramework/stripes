package net.sourceforge.stripes.chatdemo.push;

public class UserDisconnected extends PushEvent {

	private final String username;

	public UserDisconnected(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
}
