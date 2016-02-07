package net.sourceforge.stripes.chatdemo.push;

import java.util.List;

public class UsersList extends PushEvent {

	private final List<String> users;

	public UsersList(List<String> users) {
		super();
		this.users = users;
	}

	public List<String> getUsers() {
		return users;
	}
}
