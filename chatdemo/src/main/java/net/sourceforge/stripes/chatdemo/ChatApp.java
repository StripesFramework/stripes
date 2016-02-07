package net.sourceforge.stripes.chatdemo;

import net.sourceforge.stripes.action.JsonBuilder;
import net.sourceforge.stripes.chatdemo.push.*;
import net.sourceforge.stripes.controller.AsyncEvent;
import net.sourceforge.stripes.controller.AsyncListener;
import net.sourceforge.stripes.controller.AsyncResponse;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatApp implements ServletContextListener {

	private static final String CTX_NAME = "__ChatApp";

	private final Map<String,AsyncResponse> asyncResponses = new ConcurrentHashMap<String, AsyncResponse>();
	private final BlockingQueue<PushEvent> messageQueue = new LinkedBlockingQueue<PushEvent>();
	private final Notifyer notifyer = new Notifyer();

	public static ChatApp get(ServletContext context) {
		return (ChatApp)context.getAttribute(CTX_NAME);
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		sce.getServletContext().setAttribute(CTX_NAME, this);
		// start the Notifyer in a separate thread
		new Thread(notifyer).start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// stop notifyer
		notifyer.done = true;
	}

	public List<String> getAllUsers() {
		List<String> allUsers = new ArrayList<String>(asyncResponses.keySet());
		Collections.sort(allUsers);
		return allUsers;
	}

	public void connect(AsyncResponse asyncResponse) {
		HttpServletRequest request = asyncResponse.getRequest();
		String username = ChatInterceptor.getUsername(request);

		// need first initial write
		try {
			PrintWriter writer = asyncResponse.getResponse().getWriter();
			writer.write(serialize(new UsersList(getAllUsers())));
			writer.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		disconnect(request);
		asyncResponses.put(username, asyncResponse);

		asyncResponse.addListener(new AsyncListener() {

			@Override
			public void onStartAsync(AsyncEvent event) {
			}

			@Override
			public void onError(AsyncEvent event) {
				disconnect(event.getAsyncResponse().getRequest());
			}

			@Override
			public void onTimeout(AsyncEvent event) {
				disconnect(event.getAsyncResponse().getRequest());
			}

			@Override
			public void onComplete(AsyncEvent event) {
				disconnect(event.getAsyncResponse().getRequest());
			}
		});

		// broadcast to all that user has connected
		pushMessage(new UserEntered(username));
	}

	private void pushMessage(PushEvent message) {
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void disconnect(HttpServletRequest request) {
		String username = ChatInterceptor.getUsername(request);
		if (username != null) {
			if (asyncResponses.containsKey(username)) {
				asyncResponses.remove(username);
				pushMessage(new UserLeft(username));
			}
		}
	}

	public void post(HttpServletRequest request, String message) {
		String username = ChatInterceptor.getUsername(request);
		pushMessage(new MessagePosted(username, message));
	}


	private class Notifyer implements Runnable {

		private boolean done = false;

		@Override
		public void run() {
			while (!done) {
				try {
					PushEvent m = messageQueue.take();
					for (String username : asyncResponses.keySet()) {
						AsyncResponse asyncResponse = asyncResponses.get(username);
						if (asyncResponse != null) {
							try {
								PrintWriter w = asyncResponse.getResponse().getWriter();
								String msgStr = serialize(m);
								w.println(msgStr);
								w.flush();
								if (w.checkError()) {
									asyncResponses.remove(username);
									pushMessage(new UserDisconnected(username));
								}
							} catch(IOException ex) {
								ex.printStackTrace();
								asyncResponses.remove(username);
								pushMessage(new UserDisconnected(username));
							}
						}
					}


				} catch(InterruptedException iex) {
					iex.printStackTrace();
					done = true;
				}
			}
		}
	}

	private String serialize(PushEvent message) {
		String className = message.getClass().getSimpleName();
		try {
			return "<div class=\"" + className + "\">" + new JsonBuilder(message).build() + "</div>";
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
