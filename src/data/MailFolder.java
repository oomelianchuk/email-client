package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import gui.FrameManager;
import protokol.MessageContainer;

public class MailFolder implements Serializable {
	private String pathToFolder;
	public final int NUMBER_OF_MESSAGES_ON_START;
	transient private ArrayList<MessageContainer> messages;

	public void addMessage(MessageContainer message) {
		this.messages.add(message);
	}

	public void addAllMessages(ArrayList<MessageContainer> messages) {
		messages.forEach(message -> {
			if (!this.messages.contains(message)) {
				this.messages.add(message);
			}
		});
	}

	@Override
	public String toString() {
		return "MailFolder {path='" + pathToFolder + "'}";
	}

	public String getPathToFolder() {
		return pathToFolder;
	}

	public void setPathToFolder(String pathToFolder) {
		this.pathToFolder = pathToFolder;
	}

	public String getName() {
		return pathToFolder.split("/")[3];
	}

	public String getAccountName() {
		return pathToFolder.split("/")[1];
	}

	public MailFolder(String pathToFolder) {
		this.pathToFolder = pathToFolder;
		this.messages = new ArrayList<MessageContainer>();
		File folder = new File(pathToFolder);
		if (folder.listFiles() != null) {
			for (File messageFolder : folder.listFiles()) {
				try {
					if (messageFolder.exists()) {
						ObjectInputStream in = new ObjectInputStream(
								new FileInputStream(messageFolder.getAbsoluteFile() + "/message.out"));
						MessageContainer messageInfo = (MessageContainer) in.readObject();
						this.messages.add(messageInfo);
						in.close();
					}
				} catch (IOException | ClassNotFoundException e) {
					JOptionPane.showMessageDialog(null,
							FrameManager.getLanguageProperty("error.messageLoadFailedForFolder") + getName(),
							FrameManager.getLanguageProperty("error.title.messageLoadFailedForFolder"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		NUMBER_OF_MESSAGES_ON_START = messages.size();
	}

	public MailFolder(String userName, String folderName, ArrayList<MessageContainer> messages) {
		this.pathToFolder =  FrameManager.getProgramSetting("pathToUserFolders")
				.replaceAll("\\{userName\\}", userName)
				.replaceAll("\\{folderName\\}", folderName.replaceAll("\\[", "").replaceAll("\\]", ""));
		this.messages = messages;
		NUMBER_OF_MESSAGES_ON_START = messages.size();
	}

	public void serialize() throws FileNotFoundException, IOException {
		for (MessageContainer message : messages) {
			try {
				Files.createDirectories(Paths.get(message.getPath()));
				message.serialize();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public ArrayList<MessageContainer> getMessages() {
		return messages;
	}

	@Override
	public boolean equals(Object o) {
		MailFolder f = (MailFolder) o;
		return f.getPathToFolder().equals(this.getPathToFolder());
	}

}
