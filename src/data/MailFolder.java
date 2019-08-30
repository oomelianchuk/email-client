package data;

import java.util.ArrayList;

import protokol.MessageContainer;

public class MailFolder {
	private String name;
	private ArrayList<MessageContainer> messages;

	public MailFolder(String name) {
		this.name = name;
		messages = new ArrayList<MessageContainer>();
	}

	public ArrayList<MessageContainer> getMessages() {
		return messages;
	}

	public void addMessage(MessageContainer message) {
		this.messages.add(message);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "MailFolder [name=" + name + ", messages=" + messages + "]";
	}

	@Override
	public boolean equals(Object o) {
		MailFolder f = (MailFolder) o;
		return f.getName().equals(this.getName());
	}

}
