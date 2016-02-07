package net.sourceforge.stripes.chatdemo.push;

import java.util.Date;

public abstract class PushEvent {

	private final Date date;

	public PushEvent() {
		this.date = new Date();
	}

	public Date getDate() {
		return date;
	}
}
